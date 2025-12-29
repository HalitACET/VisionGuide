# VisionGuide API

VisionGuide projesi için bulut tabanlı nesne tespiti API'si. FastAPI, TensorFlow Hub ve EfficientDet modeli kullanarak Android uygulamalarından gelen görüntüleri analiz eder.

## Özellikler

- 🚀 **FastAPI** tabanlı modern REST API
- 🤖 **EfficientDet D0** modeli ile yüksek performanslı nesne tespiti
- 📱 **Android uyumlu** Base64 görüntü desteği
- 🔍 **80 farklı nesne sınıfı** tespiti (COCO veri seti)
- ⚡ **Async/await** desteği ile yüksek performans
- 📊 **Pydantic** ile tip güvenli veri modelleri
- 🌐 **CORS** desteği

## Kurulum

1. **Gerekli kütüphaneleri yükleyin:**

```bash
pip install -r requirements.txt
```

2. **API'yi başlatın:**

```bash
python main.py
```

API `http://localhost:8000` adresinde çalışmaya başlayacak.

## API Endpoints

### POST /detect

Nesne tespiti yapar.

**Request Body:**

```json
{
  "image": "base64_encoded_image_string"
}
```

**Response:**

```json
{
  "detections": [
    {
      "label": "person",
      "score": 0.95,
      "box": [0.1, 0.2, 0.8, 0.9]
    }
  ],
  "total_detections": 1
}
```

### GET /health

API sağlık kontrolü.

### GET /

Ana sayfa ve endpoint bilgileri.

## Kullanım Örneği

```python
import requests
import base64

# Görüntüyü Base64'e çevir
with open("image.jpg", "rb") as f:
    image_base64 = base64.b64encode(f.read()).decode()

# API'ye istek gönder
response = requests.post(
    "http://localhost:8000/detect",
    json={"image": image_base64}
)

result = response.json()
print(f"Tespit edilen nesne sayısı: {result['total_detections']}")
```

## Teknik Detaylar

- **Model:** EfficientDet D0 (TensorFlow Hub)
- **Sınıf Sayısı:** 80 (COCO veri seti)
- **Güven Skoru:** 0.3 threshold
- **Görüntü Formatı:** RGB, Base64
- **Bounding Box:** Normalized koordinatlar [x1, y1, x2, y2]

## Geliştirme

API'yi geliştirme modunda çalıştırmak için:

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

## Lisans

MIT License


