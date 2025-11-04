"""
VisionGuide API - FastAPI tabanlı nesne tespiti servisi
EfficientDet D0 modeli kullanarak görüntülerde nesne tespiti yapar.
"""

import base64
import io
import logging
from typing import List, Dict, Any
import numpy as np
from PIL import Image
import tensorflow as tf
import tensorflow_hub as hub
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from labels import get_label_name

# Logging ayarları
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPI uygulaması
app = FastAPI(
    title="VisionGuide API",
    description="EfficientDet D0 modeli ile nesne tespiti API'si",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Production'da spesifik domain'ler belirtin
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global değişkenler
detector = None
detection_threshold = 0.65  # 0.65'e yükseltildi - daha güvenilir ve doğru tespitler için

# Pydantic modelleri
class DetectionRequest(BaseModel):
    image: str  # Base64 encoded image

class Detection(BaseModel):
    label: str
    score: float
    box: List[float]  # [x1, y1, x2, y2] normalized coordinates

class DetectionResponse(BaseModel):
    detections: List[Detection]
    total_detections: int

class HealthResponse(BaseModel):
    status: str
    model_loaded: bool

def load_model():
    """EfficientDet D0 modelini yükler"""
    global detector
    try:
        logger.info("EfficientDet D0 modeli yükleniyor...")
        model_url = "https://tfhub.dev/tensorflow/efficientdet/d0/1"
        detector = hub.load(model_url)
        logger.info("Model başarıyla yüklendi!")
        return True
    except Exception as e:
        logger.error(f"Model yükleme hatası: {e}")
        return False

def preprocess_image(image_data: bytes) -> np.ndarray:
    """Görüntüyü model için hazırlar"""
    try:
        # PIL Image'a çevir
        image = Image.open(io.BytesIO(image_data))
        
        # RGB formatına çevir
        if image.mode != 'RGB':
            image = image.convert('RGB')
        
        # NumPy array'e çevir
        image_array = np.array(image)
        
        # TensorFlow tensor'a çevir
        image_tensor = tf.convert_to_tensor(image_array, dtype=tf.uint8)
        
        # Batch dimension ekle (model 4 boyutlu tensor bekliyor: [batch, height, width, channels])
        image_tensor = tf.expand_dims(image_tensor, 0)
        
        return image_tensor
    except Exception as e:
        logger.error(f"Görüntü işleme hatası: {e}")
        raise HTTPException(status_code=400, detail="Geçersiz görüntü formatı")

def postprocess_detections(detections: Dict[str, Any]) -> List[Detection]:
    """Model çıktısını API formatına çevirir"""
    try:
        detection_results = []
        
        # Model çıktısından detection'ları al
        # TensorFlow Hub model çıktısı farklı format olabilir
        if isinstance(detections, dict):
            boxes = detections.get('detection_boxes', [])
            scores = detections.get('detection_scores', [])
            classes = detections.get('detection_classes', [])
        else:
            # Eğer detections bir tensor ise
            boxes = detections[0] if len(detections) > 0 else []
            scores = detections[1] if len(detections) > 1 else []
            classes = detections[2] if len(detections) > 2 else []
        
        # Tensor'ları numpy array'e çevir
        if hasattr(boxes, 'numpy'):
            boxes = boxes.numpy()
        if hasattr(scores, 'numpy'):
            scores = scores.numpy()
        if hasattr(classes, 'numpy'):
            classes = classes.numpy()
        
        # Batch dimension'ı kaldır (eğer varsa)
        if isinstance(boxes, np.ndarray) and len(boxes.shape) > 1:
            if boxes.shape[0] == 1:
                boxes = boxes[0]  # (1, N, 4) -> (N, 4)
        if isinstance(scores, np.ndarray) and len(scores.shape) > 1:
            if scores.shape[0] == 1:
                scores = scores[0]  # (1, N) -> (N,)
        if isinstance(classes, np.ndarray) and len(classes.shape) > 1:
            if classes.shape[0] == 1:
                classes = classes[0]  # (1, N) -> (N,)
        
        # Eğer boş ise, boş liste döndür
        if isinstance(scores, np.ndarray):
            if scores.size == 0:
                return detection_results
            num_detections = scores.shape[0]
        else:
            if len(scores) == 0:
                return detection_results
            num_detections = len(scores)
        
        # Detection'ları işle
        for i in range(num_detections):
            score = float(scores[i]) if isinstance(scores, np.ndarray) else float(scores[i])
            
            if score >= detection_threshold:
                class_id = int(classes[i]) if isinstance(classes, np.ndarray) else int(classes[i])
                label = get_label_name(class_id)
                
                # Box formatını kontrol et ve dönüştür
                # EfficientDet genellikle [y1, x1, y2, x2] formatında döner
                # Biz [x1, y1, x2, y2] formatını bekliyoruz
                if isinstance(boxes, np.ndarray):
                    box = boxes[i]
                    if hasattr(box, 'tolist'):
                        box_list = box.tolist()
                    else:
                        box_list = list(box)
                else:
                    box_list = list(boxes[i])
                
                # Box formatını [x1, y1, x2, y2] formatına çevir
                if len(box_list) >= 4:
                    # Eğer [y1, x1, y2, x2] formatındaysa
                    y1, x1, y2, x2 = box_list[:4]
                    box_list = [x1, y1, x2, y2]
                
                detection = Detection(
                    label=label,
                    score=score,
                    box=box_list
                )
                detection_results.append(detection)
        
        return detection_results
    except Exception as e:
        logger.error(f"Detection işleme hatası: {e}")
        import traceback
        logger.error(traceback.format_exc())
        # Hata durumunda boş liste döndür
        return []

@app.on_event("startup")
async def startup_event():
    """Uygulama başlatıldığında modeli yükle"""
    logger.info("VisionGuide API başlatılıyor...")
    model_loaded = load_model()
    if not model_loaded:
        logger.error("Model yüklenemedi! API çalışmayabilir.")

@app.get("/", response_model=Dict[str, Any])
async def root():
    """Ana sayfa - API bilgileri"""
    return {
        "message": "VisionGuide API",
        "version": "1.0.0",
        "endpoints": {
            "detect": "POST /detect - Nesne tespiti yapar",
            "health": "GET /health - API sağlık kontrolü"
        },
        "model_info": {
            "name": "EfficientDet D0",
            "classes": 80,
            "threshold": detection_threshold
        }
    }

@app.get("/health", response_model=HealthResponse)
async def health_check():
    """API sağlık kontrolü"""
    model_loaded = detector is not None
    return HealthResponse(
        status="healthy" if model_loaded else "model_not_loaded",
        model_loaded=model_loaded
    )

@app.post("/detect", response_model=DetectionResponse)
async def detect_objects(request: DetectionRequest):
    """Nesne tespiti yapar"""
    try:
        # Model yüklenmiş mi kontrol et
        if detector is None:
            raise HTTPException(status_code=503, detail="Model henüz yüklenmedi")
        
        # Base64'ü decode et
        try:
            image_data = base64.b64decode(request.image)
        except Exception:
            raise HTTPException(status_code=400, detail="Geçersiz Base64 formatı")
        
        # Görüntüyü işle
        image_tensor = preprocess_image(image_data)
        
        # Detection yap
        logger.info("Nesne tespiti yapılıyor...")
        
        try:
            detections = detector(image_tensor)
            logger.info(f"Model çıktısı tipi: {type(detections)}")
            
            # Sonuçları işle
            detection_results = postprocess_detections(detections)
            
        except Exception as model_error:
            logger.error(f"Model çalıştırma hatası: {model_error}")
            # Model hatası durumunda boş sonuç döndür
            detection_results = []
        
        logger.info(f"{len(detection_results)} nesne tespit edildi")
        
        # Debug: Tespit edilen nesneleri logla
        if detection_results:
            for det in detection_results:
                logger.info(f"  - {det.label}: {det.score:.2f}")
        
        return DetectionResponse(
            detections=detection_results,
            total_detections=len(detection_results)
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Detection hatası: {e}")
        raise HTTPException(status_code=500, detail="İç sunucu hatası")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
