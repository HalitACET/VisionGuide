# Python 3.11 tabanlı resmi imajı kullan
FROM python:3.11-slim

# Çalışma dizinini ayarla
WORKDIR /app

# Sistem bağımlılıklarını yükle (ffmpeg ses işleme için gerekli olabilir)
RUN apt-get update && apt-get install -y \
    ffmpeg \
    libsm6 \
    libxext6 \
    && rm -rf /var/lib/apt/lists/*

# Gereksinimleri kopyala ve yükle
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Tüm kodları kopyala
COPY . .

# Portu dışarı aç
EXPOSE 8000

# Uygulamayı başlat
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
