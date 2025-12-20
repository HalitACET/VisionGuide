import sys
import traceback
from sqlalchemy import create_engine, inspect
from main import PostModel, DATABASE_URL

print(f"Connecting to: {DATABASE_URL}")

try:
    engine = create_engine(DATABASE_URL)
    inspector = inspect(engine)
    
    if inspector.has_table("posts"):
        print("Table 'posts' exists.")
        columns = [c['name'] for c in inspector.get_columns("posts")]
        print(f"Columns: {columns}")
        
        if "audio_url" not in columns:
            print("CRITICAL: 'audio_url' column is MISSING!")
    else:
        print("Table 'posts' does NOT exist.")

    # Try simple query
    from sqlalchemy.orm import sessionmaker
    SessionLocal = sessionmaker(bind=engine)
    db = SessionLocal()
    print("Attempting to query posts...")
    posts = db.query(PostModel).all()
    print(f"Success! Found {len(posts)} posts.")
    db.close()

except Exception:
    print("Database Error Occurred:")
    traceback.print_exc()
