"""
VisionGuide API - FastAPI tabanlı nesne tespiti servisi
EfficientDet D0 modeli kullanarak görüntülerde nesne tespiti yapar.
"""

import base64
import os
import io
import logging
from typing import List, Dict, Any, Optional
import numpy as np
from PIL import Image
try:
    import pytesseract
except ImportError:
    pytesseract = None

# TensorFlow removed - we use YOLOv8 and Gemini

from fastapi import FastAPI, HTTPException, File, UploadFile
from fastapi import status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sqlalchemy.orm import Session
from datetime import datetime
from typing import List, Optional, Any, Dict
from labels import get_label_name
from yolo_engine import yolo_engine

from gemini_engine import GeminiEngine


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



gemini_engine = None

detection_threshold = 0.65  # 0.65'e yükseltildi - daha güvenilir ve doğru tespitler için

# Pydantic modelleri
class DetectionRequest(BaseModel):
    image: str  # Base64 encoded image

class Detection(BaseModel):
    label: str
    score: float
    box: List[float]  # [x1, y1, x2, y2] normalized coordinates
    position_desc: str = "Bilinmiyor"
    proximity_desc: str = "Bilinmiyor"

class DetectionResponse(BaseModel):
    detections: List[Detection]
    total_detections: int

class HealthResponse(BaseModel):
    status: str
    model_loaded: bool




class ImageUploadRequest(BaseModel):
    image: str

class SegmentRequest(BaseModel):
    image: str
    prompt: str

class OcrResponse(BaseModel):
    text: str

class AnalyzeRequest(BaseModel):
    image: str
    feature: str # "currency" or "color"

class AnalyzeResponse(BaseModel):
    result: str




class AskGeminiRequest(BaseModel):
    image: str
    prompt: str

class AskGeminiResponse(BaseModel):
    answer: str

class DescribeSceneRequest(BaseModel):
    image: str

class DescribeSceneResponse(BaseModel):
    description: str



from sqlalchemy import create_engine, Column, Integer, String, Text
from sqlalchemy import text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from fastapi import Depends
from dotenv import load_dotenv

from jose import jwt
from passlib.context import CryptContext

load_dotenv()

# Database Setup
DATABASE_URL = os.getenv("DATABASE_URL")
if not DATABASE_URL:
    # Fallback for testing if env not set (User should set this!)
    logger.warning("DATABASE_URL not found, using local sqlite for fallback.")
    DATABASE_URL = "sqlite:///./visionguide.db"

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY") or "dev-secret-change-me"
JWT_ALGORITHM = "HS256"
JWT_EXPIRES_MINUTES = int(os.getenv("JWT_EXPIRES_MINUTES") or "10080")

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()



from sqlalchemy import create_engine, Column, Integer, String, Text, ForeignKey, DateTime
from sqlalchemy.sql import func
from sqlalchemy.orm import sessionmaker, declarative_base, relationship
import boto3
from botocore.exceptions import NoCredentialsError

# S3 Setup
S3_BUCKET_NAME = os.getenv("S3_BUCKET_NAME")
s3_client = boto3.client('s3')

def sign_s3_url(url: str):
    """
    Converts a standard S3 URL to a presigned URL for temporary access.
    """
    if not url or not S3_BUCKET_NAME:
        return url
    
    try:
        # Expected format: https://{BUCKET}.s3.amazonaws.com/{KEY}
        prefix = f"https://{S3_BUCKET_NAME}.s3.amazonaws.com/"
        
        if url.startswith(prefix):
            key = url.replace(prefix, "")
            # Generate presigned URL
            signed_url = s3_client.generate_presigned_url(
                'get_object',
                Params={'Bucket': S3_BUCKET_NAME, 'Key': key},
                ExpiresIn=3600  # 1 hour validity
            )
            return signed_url
    except Exception as e:
        logger.error(f"Error signing URL: {e}")
    
    return url

# ORM Model
class PostModel(Base):
    __tablename__ = "posts"
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    content = Column(Text)
    author = Column(String)
    audio_url = Column(String, nullable=True) # New field
    comments = relationship("CommentModel", back_populates="post", cascade="all, delete-orphan")

class CommentModel(Base):
    __tablename__ = "comments"
    id = Column(Integer, primary_key=True, index=True)
    post_id = Column(Integer, ForeignKey("posts.id"))
    content = Column(Text)
    author = Column(String)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    post = relationship("PostModel", back_populates="comments")


class UserModel(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    password_hash = Column(String, nullable=False)
    first_name = Column(String, nullable=False, default="")
    last_name = Column(String, nullable=False, default="")
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class AuthRegisterRequest(BaseModel):
    email: str
    password: str
    first_name: str
    last_name: str


class AuthLoginRequest(BaseModel):
    email: str
    password: str


class AuthResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    email: str
    first_name: str
    last_name: str

# Pydantic Model Updates
class Post(BaseModel):
    id: int
    title: str
    content: str
    author: str
    audio_url: Optional[str] = None # New field

class CreatePostRequest(BaseModel):
    title: str
    content: str
    author: str = "Anonymous"
    audio_url: Optional[str] = None # New field

class CommentCreate(BaseModel):
    content: str
    author: str = "Anonymous"

class CommentResponse(BaseModel):
    id: int
    post_id: int
    content: str
    author: str
    created_at: datetime

    class Config:
        orm_mode = True

# Create tables (will update schema if needed, but for existing tables usually requires migration. For dev, we might need to recreate or alter)
Base.metadata.create_all(bind=engine)


def ensure_user_columns():
    try:
        if not str(DATABASE_URL).startswith("sqlite"):
            return

        with engine.connect() as conn:
            cols = [row[1] for row in conn.execute(text("PRAGMA table_info(users)"))]
            if "first_name" not in cols:
                conn.execute(text("ALTER TABLE users ADD COLUMN first_name VARCHAR DEFAULT '' NOT NULL"))
            if "last_name" not in cols:
                conn.execute(text("ALTER TABLE users ADD COLUMN last_name VARCHAR DEFAULT '' NOT NULL"))
            conn.commit()
    except Exception as e:
        logger.error(f"User table migration error: {e}")


def create_access_token(email: str) -> str:
    from datetime import timedelta
    from datetime import datetime as _dt

    expire = _dt.utcnow() + timedelta(minutes=JWT_EXPIRES_MINUTES)
    payload = {
        "sub": email,
        "exp": expire,
    }
    return jwt.encode(payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)


@app.post("/auth/register", response_model=AuthResponse)
async def auth_register(request: AuthRegisterRequest, db: Session = Depends(get_db)):
    email = request.email.strip().lower()
    password = request.password
    first_name = request.first_name.strip()
    last_name = request.last_name.strip()

    if not email or not password or not first_name or not last_name:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid credentials")

    existing = db.query(UserModel).filter(UserModel.email == email).first()
    if existing:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Email already exists")

    user = UserModel(
        email=email,
        password_hash=pwd_context.hash(password),
        first_name=first_name,
        last_name=last_name
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token(email=email)
    return AuthResponse(
        access_token=token,
        email=email,
        first_name=user.first_name,
        last_name=user.last_name
    )


@app.post("/auth/login", response_model=AuthResponse)
async def auth_login(request: AuthLoginRequest, db: Session = Depends(get_db)):
    email = request.email.strip().lower()
    password = request.password

    if not email or not password:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid credentials")

    user = db.query(UserModel).filter(UserModel.email == email).first()
    if not user or not pwd_context.verify(password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized")

    token = create_access_token(email=email)
    return AuthResponse(
        access_token=token,
        email=email,
        first_name=user.first_name,
        last_name=user.last_name
    )

@app.post("/upload-audio")
async def upload_audio(file: UploadFile = File(...)):
    """Uploads an audio file to S3 and returns the URL."""
    if not S3_BUCKET_NAME:
        raise HTTPException(status_code=500, detail="S3 Bucket not configured")
    
    try:
        # Generate a unique filename
        import uuid
        file_extension = file.filename.split(".")[-1]
        filename = f"{uuid.uuid4()}.{file_extension}"
        
        # Upload to S3
        s3_client.upload_fileobj(
            file.file,
            S3_BUCKET_NAME,
            filename,
            ExtraArgs={'ContentType': file.content_type}
        )
        
        # Construct URL
        # Region usually defaults to us-east-1 if not specified, but let's assume standard format
        # Better: Get location from client or assume standard
        file_url = f"https://{S3_BUCKET_NAME}.s3.amazonaws.com/{filename}"
        
        return {"url": file_url}
    except NoCredentialsError:
         raise HTTPException(status_code=500, detail="AWS Credentials not found")
    except Exception as e:
        logger.error(f"S3 Upload Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/posts", response_model=List[Post])
async def get_posts(db: Session = Depends(get_db)):
    posts = db.query(PostModel).all()
    # Handle possible missing column in old data by defaulting to None if using raw object, but ORM handles it
    # Sign URLs before returning
    return [
        Post(
            id=p.id, 
            title=p.title, 
            content=p.content, 
            author=p.author, 
            audio_url=sign_s3_url(p.audio_url)
        ) for p in posts
    ]

@app.get("/posts/{post_id}", response_model=Post)
async def get_post(post_id: int, db: Session = Depends(get_db)):
    post = db.query(PostModel).filter(PostModel.id == post_id).first()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")
    
    return Post(
        id=post.id, 
        title=post.title, 
        content=post.content, 
        author=post.author, 
        audio_url=sign_s3_url(post.audio_url)
    )

@app.get("/posts/{post_id}/comments", response_model=List[CommentResponse])
async def get_comments(post_id: int, db: Session = Depends(get_db)):
    comments = db.query(CommentModel).filter(CommentModel.post_id == post_id).all()
    return comments

@app.post("/posts/{post_id}/comments", response_model=CommentResponse)
async def create_comment(post_id: int, comment: CommentCreate, db: Session = Depends(get_db)):
    # Check if post exists
    post = db.query(PostModel).filter(PostModel.id == post_id).first()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")
        
    db_comment = CommentModel(
        post_id=post_id,
        content=comment.content,
        author=comment.author
    )
    db.add(db_comment)
    db.commit()
    db.refresh(db_comment)
    return db_comment

@app.post("/posts", response_model=Post)
async def create_post(request: CreatePostRequest, db: Session = Depends(get_db)):
    db_post = PostModel(
        title=request.title, 
        content=request.content, 
        author=request.author,
        audio_url=request.audio_url
    )
    db.add(db_post)
    db.commit()
    db.refresh(db_post)
    db.refresh(db_post)
    
    # Return signed URL in response so it works immediately
    return Post(
        id=db_post.id, 
        title=db_post.title, 
        content=db_post.content, 
        author=db_post.author, 
        audio_url=sign_s3_url(db_post.audio_url)
    )

@app.post("/analyze", response_model=AnalyzeResponse)
async def analyze_image(request: AnalyzeRequest):

    """Analyze endpoint for Currency and Color using Gemini"""
    logger.info(f"Analyze request received for feature: {request.feature}")
    
    try:
        # Currency Recognition with Gemini
        if request.feature == "currency":
            if gemini_engine and gemini_engine.model:
                image_bytes = base64.b64decode(request.image)
                prompt = (
                    "Bu resimdeki paraları tanımla. Kağıt ve madeni paraları ayırt et. "
                    "Her bir paranın değerini listele. "
                    "Hesaplamanı yap ve en sonda 'Toplam Tutar: X TL' şeklinde net bir toplam yaz. "
                    "Cevabını SADECE DÜZ METİN olarak ver. Markdown, emoji veya * # gibi semboller KULLANMA. "
                    "Görme engelli bir kullanıcı için sadece paralarla ilgili net ve anlaşılır bilgi ver."
                )
                result = gemini_engine.generate_content(image_bytes, prompt)
            else:
                logger.warning("Gemini engine not available, falling back to mock.")
                result = "50 Türk Lirası (Mock - API Anahtarı Ayarlanmadı)"
            
            return AnalyzeResponse(result=result)

        elif request.feature == "color":
             # Color recognition could also be moved to Gemini easily, but keeping mock/simple for now as requested only currency
            return AnalyzeResponse(result="Kırmızı")
        else:
            return AnalyzeResponse(result="Bilinmeyen özellik")

    except Exception as e:
        logger.error(f"Analysis error: {e}")
        raise HTTPException(status_code=500, detail=str(e))




@app.post("/ocr", response_model=OcrResponse)
async def read_text(request: ImageUploadRequest):
    """Gerçek OCR endpoint'i.

    
    Gemini API kullanarak görseldeki metni okur.
    """
    logger.info("OCR request received")

    try:
        # Base64'ü çöz
        image_bytes = base64.b64decode(request.image)
        
        # Gemini ile metin okuma
        if gemini_engine and gemini_engine.model:
            prompt = "Görüntüdeki tüm metni olduğu gibi, satır satır oku. Yorum yapma, sadece metni ver."
            text = gemini_engine.generate_content(image_bytes, prompt)
        else:
            # Fallback (Eğer Gemini yoksa)
            logger.warning("Gemini engine not available for OCR, falling back to mock.")
            text = "Gemini API anahtarı bulunamadı. Lütfen ayarlayın."


        return OcrResponse(text=text)
    except Exception as e:
        logger.error(f"OCR processing error: {e}")
        raise HTTPException(status_code=500, detail="OCR işlemi sırasında hata oluştu")



@app.post("/ask_gemini", response_model=AskGeminiResponse)
async def ask_gemini(request: AskGeminiRequest):
    """Gemini API kullanarak görüntü hakkında soru sorar."""
    try:
        image_bytes = base64.b64decode(request.image)
        # Enhanced prompt for accessibility with spatial awareness
        system_instruction = (
            "Sen görme engelli bir birey için yardımcı asistansın. "
            "Cevabını sadece düz metin olarak ver. ASLA Markdown (yıldız, kare, tire vb.) veya emoji kullanma. "
            "Sembol işaretleri kullanma çünkü bu metin sesli olarak okunacak. "
            "Kısa, net ve anlaşılır ol. "
            "Eğer sorulursa, nesnelerin konumunu (sağda, solda, üstte, uzakta) ve birbirlerine göre yerlerini belirt. "
            f"Kullanıcı sorusu: {request.prompt}"
        )
        answer = gemini_engine.generate_content(image_bytes, system_instruction)
        return AskGeminiResponse(answer=answer)
    except Exception as e:
        logger.error(f"Gemini request error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/describe_scene", response_model=DescribeSceneResponse)
async def describe_scene(request: DescribeSceneRequest):
    """Görüntüdeki sahneyi görme engelli bir kullanıcı için betimler."""
    logger.info("Scene description request received")
    try:
        image_bytes = base64.b64decode(request.image)
        
        if gemini_engine and gemini_engine.model:
            prompt = (
                "Sen görme engelli bir birey için akıllı bir görsel asistansın. "
                "Bu resimdeki sahneyi betimle. Aşağıdaki detaylara ÖNEMLE dikkat et:\n"
                "1. Nesnelerin kullanıcıya göre konumu (örneğin: 'sağınızda', 'solunuzda', 'hemen önünüzde').\n"
                "2. Nesnelerin birbirine göre konumu (örneğin: 'masanın üzerinde', 'sandalyenin yanında').\n"
                "3. Cevabını SADECE DÜZ METİN olarak ver. Markdown, emoji veya * # - gibi semboller ASLA KULLANMA. \n"
                "4. Metin sesli okunacağı için akıcı, anlaşılır ve yardımsever bir dil kullan. \n"
                "5. Ortamı zihninde canlandırabilmesi için detaylı ama karmaşık olmayan bir anlatım yap."
            )
            description = gemini_engine.generate_content(image_bytes, prompt)
        else:
            logger.warning("Gemini engine not available for scene description.")
            description = "Sahne betimleme servisi kullanılamıyor."
            
        return DescribeSceneResponse(description=description)
    except Exception as e:
        logger.error(f"Scene description error: {e}")
        raise HTTPException(status_code=500, detail=str(e))



def init_db_data(db: Session):
    if db.query(PostModel).count() == 0:
        db.add(PostModel(title="Merhaba!", content="VisionGuide topluluğuna hoş geldiniz.", author="Admin"))
        db.add(PostModel(title="Renkler", content="Kırmızı ve yeşili ayırt etmekte zorlanıyorum, ipucu var mı?", author="User123"))
        db.commit()

@app.on_event("startup")
async def startup_event():
    """Uygulama başlatıldığında modelleri yükle"""
    logger.info("VisionGuide API başlatılıyor...")
    
    # Initialize Database Data (if empty)
    try:
        ensure_user_columns()
        db = SessionLocal()
        init_db_data(db)
        db.close()
    except Exception as e:
        logger.error(f"Database initialization error: {e}")
    


    # Load YOLOv8 (nesne tespiti için)
    yolo_loaded = yolo_engine.load_model()
    if not yolo_loaded:
        logger.error("YOLOv8 model yüklenemedi. 'pip install ultralytics' gerekli olabilir.")


    # Initialize Gemini Engine
    global gemini_engine
    gemini_engine = GeminiEngine()


    # SAM 3 is mocked
    logger.info("SAM 3 Mock Mode aktif.")

@app.post("/segment")
async def segment_objects(request: SegmentRequest):
    """SAM 3 ile metin tabanlı segmentasyon yapar (Mock Mode)"""
    try:
        # Mock response for testing visualization
        # Returns a box polygon: [[100, 100], [400, 100], [400, 400], [100, 400]]
        logger.info(f"Mocking segmentation for prompt: {request.prompt}")
        
        mock_results = [
            {
                "label": request.prompt,
                "score": 0.99,
                "mask": [[100, 100], [400, 100], [400, 400], [100, 400]]
            }
        ]
        
        return {"results": mock_results}
    except Exception as e:
        logger.error(f"Segmentation error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/detect", response_model=DetectionResponse)
async def detect_objects(request: DetectionRequest):
    """YOLOv8 ile nesne tespiti yapar."""
    try:
        image_bytes = base64.b64decode(request.image)
    except Exception:
        raise HTTPException(status_code=400, detail="Geçersiz base64 görüntü")

    detections_raw = yolo_engine.predict(image_bytes)
    detections = []
    for d in detections_raw:
        box = d["bbox"] # [x1, y1, x2, y2]
        
        # Spatial Context Logic
        x1, y1, x2, y2 = box
        center_x = (x1 + x2) / 2
        area = (x2 - x1) * (y2 - y1)
        
        # Position Description
        if center_x < 0.33:
            pos_desc = "Sol"
        elif center_x > 0.66:
            pos_desc = "Sağ"
        else:
            pos_desc = "Orta"
            
        # Proximity Description (Area based estimation)
        # These thresholds might need tuning based on camera FOV
        if area > 0.15:
            prox_desc = "Yakın"
        elif area < 0.05:
            prox_desc = "Uzak"
        else:
            prox_desc = "Orta Mesafe"

        detections.append(
            Detection(
                label=d["label"], 
                score=float(d["score"]), 
                box=box,
                position_desc=pos_desc,
                proximity_desc=prox_desc
            )
        )

    return DetectionResponse(detections=detections, total_detections=len(detections))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
