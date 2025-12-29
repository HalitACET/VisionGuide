from sqlalchemy import create_engine, text
from main import DATABASE_URL

print(f"Connecting to DB to fix schema...")
engine = create_engine(DATABASE_URL)

with engine.connect() as conn:
    print("Adding 'audio_url' column to 'posts' table...")
    try:
        # PostgreSQL specific syntax (IF NOT EXISTS is nice but standard is just ADD)
        # Using raw SQL
        conn.execute(text("ALTER TABLE posts ADD COLUMN IF NOT EXISTS audio_url VARCHAR;"))
        conn.commit()
        print("Success! Column added.")
    except Exception as e:
        print(f"Error: {e}")
        
    # Verify
    try:
        result = conn.execute(text("SELECT audio_url FROM posts LIMIT 1;"))
        print("Verification: Column exists.")
    except Exception as e:
        print(f"Verification Failed: {e}")
