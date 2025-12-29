import logging
import gc
import threading
from typing import List, Dict, Any, Union, Optional
import io
from PIL import Image
import numpy as np
import torch
import asyncio

from vocabulary import MODES, OBJ_MAP

try:
    from ultralytics import YOLO
    from ultralytics.nn.tasks import DetectionModel
except ImportError:
    YOLO = None
    DetectionModel = None

logger = logging.getLogger(__name__)

# Model config
MODEL_NAME = "yolov8m.pt"
CONFIDENCE_THRESHOLD = 0.35

class YoloEngine:
    def __init__(self, model_path: str = MODEL_NAME):
        """YOLOv8 tabanlı nesne tespiti motoru."""
        self.model_path = model_path
        self.model = None
        self._model_lock = threading.Lock()
        self._current_mode = None

    def load_model(self) -> bool:
        """Modeli yükler. YOLO paketi yoksa veya hata olursa False döner."""
        if YOLO is None:
            logger.error("Ultralytics YOLO paketi bulunamadı. 'pip install ultralytics' ile kurun.")
            return False
            
        with self._model_lock:
            try:
                if self.model is None:
                    logger.info(f"YOLOv8 modeli yükleniyor: {self.model_path}")
                    
                    # Safe globals hack for torch signaling
                    try:
                        if DetectionModel is not None and hasattr(torch, "serialization") and hasattr(
                            torch.serialization, "add_safe_globals"
                        ):
                            torch.serialization.add_safe_globals([DetectionModel])
                    except Exception as e:
                        logger.warning(f"YOLOv8 safe_globals kaydı sırasında hata oluştu: {e}")

                    self.model = YOLO(self.model_path)
                    logger.info("YOLOv8 modeli başarıyla yüklendi.")
                    
                return True
            except Exception as e:
                logger.error(f"YOLOv8 model yükleme hatası: {e}")
                return False

    def _ensure_model(self) -> bool:
        if self.model is not None:
            return True
        return self.load_model()
        
    def cleanup_memory(self):
        """Bellek temizliği"""
        gc.collect()
        if torch.cuda.is_available():
            torch.cuda.empty_cache()

    def predict(self, image: Union[bytes, Image.Image, np.ndarray], mode: str = "E") -> List[Dict[str, Any]]:
        """Görüntüde nesne tespiti yapar.
        
        Args:
            image: Görüntü verisi
            mode: Tespit modu ('E'v, 'S'okak, 'M'arket)
        """
        if not self._ensure_model():
            logger.error("YOLO modeli yüklenemedi, boş sonuç döndürülüyor.")
            return []

        try:
            # Görüntü hazırlığı
            if isinstance(image, bytes):
                img = Image.open(io.BytesIO(image)).convert("RGB")
            elif isinstance(image, Image.Image):
                img = image.convert("RGB")
            else:
                img = image # numpy array assumed

            # Mod ayarı (set_classes ile filtreleme)
            with self._model_lock:
                if mode in MODES:
                    target_classes = MODES[mode]['classes']
                    # Not: YOLOv8 model.predict(classes=[...]) parametresini destekler.
                    # Ancak burada model state'ini değiştirmek yerine predict çağrısında filtrelemek daha güvenli olabilir.
                    # Fakat performans için sınıfları önceden belirlemek gerekebilir.
                    # Şimdilik post-filtering yapacağız çünkü standart YOLOv8 modelinde 'set_classes' her zaman mevcut değil.
                    # Ancak Tevfik'in kodunda set_classes kullanılmış, biz post-process filtreleme ile daha güvenli ilerleyeceğiz
                    # çünkü base model sınıfları (COCO) ile bizim kelime listemiz (vocabulary) tam birebir ID eşleşmeyebilir.
                    # DÜZELTME: Tevfik'in kodunda 'yolov8n-world.pt' kullanılıyor ki bu açık uçlu (open-vocabulary) bir model.
                    # Eğer biz standart yolov8m.pt kullanıyorsak (COCO 80 sınıf), o zaman Tevfik'in 500 kelimesini destekleyemeyiz.
                    # Tevfik'in bahsettiği "500 nesne" özelliği için yolov8-world veya custom eğitilmiş model gerekir.
                    # Ancak mevcut 'yolov8m.pt' sadece COCO sınıflarını tanır.
                    # Tevfik'in kodunda model = YOLO('yolov8n-world.pt') var.
                    # Bizim de world modele geçmemiz lazım EĞER o dosya varsa. Yoksa standart modelle devam edip maplemeye çalışacağız.
                    pass

            # Standart YOLOv8 COCO sınıfları ile tahmin (şimdilik)
            # Eğer world modeli indirip kullanırsak set_classes işe yarar.
            # Güvenlik için classes parametresini kullanmıyoruz, tümünü alıp Python tarafında filtreliyoruz.
            results = self.model(img, conf=CONFIDENCE_THRESHOLD, verbose=False)[0]
            
            detections: List[Dict[str, Any]] = []
            
            # Modun izin verdiği kelimeler
            if mode in MODES:
                allowed_vocab = set(MODES[mode]['classes'])
            else:
                allowed_vocab = set()

            for box in results.boxes:
                cls_id = int(box.cls[0])
                label_en = results.names.get(cls_id, str(cls_id))
                
                # Standart YOLOv8 (COCO) kullanıyorsak, label_en zaten COCO sınıfıdır (örn: 'person', 'cup').
                # Bizim vocabulary.py dosyamızda bu kelimelerin Türkçesi var.
                
                # Mod filtresi: Eğer mod aktifse ve bulunan nesne o modun listesinde yoksa ele.
                # Ancak COCO sınıfları sınırlı (80 tane). Tevfik 500 kelime diyorsa muhtemelen World model kullanmalıydık.
                # Fakat User sadece entegre et dedi, dosya indir demedi.
                # Biz elimizdeki modelle (yolov8m) en iyisini yapacağız.
                # Eğer Tevfik'in attığı update içinde model dosyası yoksa, standart model kullanılıyor demektir.
                # Tevfik'in kodunda 'yolov8n-world.pt' referansı vardı. Biz bunu destekleyelim.
                
                # Türkçe karşılığı
                label_tr = OBJ_MAP.get(label_en, label_en)
                
                # Eğer modda bu kelime (ingilizce anahtarı) tanımlıysa ekle.
                # Eğer allowed_vocab boşsa (geçersiz mod), hepsini ekle ya da hiçbirini ekleme? Var sayılan E modu olsun.
                if allowed_vocab and label_en not in allowed_vocab:
                     continue

                score = float(box.conf[0])
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                
                detections.append({
                    "label": label_tr, # Türkçe döndür
                    "label_en": label_en,
                    "score": score,
                    "bbox": [x1, y1, x2, y2],
                })

            self.cleanup_memory()
            return detections
            
        except Exception as e:
            logger.error(f"YOLOv8 prediction error: {e}")
            self.cleanup_memory()
            return []

# Global engine örneği
yolo_engine = YoloEngine()
