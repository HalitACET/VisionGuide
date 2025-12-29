import asyncio
import base64
import sys
import os

# Ensure backend directory is in path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from yolo_engine import YoloEngine
from vocabulary import MODES

def create_dummy_image_base64():
    """Creates a small red dummy image in base64."""
    from PIL import Image
    import io
    img = Image.new('RGB', (100, 100), color = 'red')
    buffered = io.BytesIO()
    img.save(buffered, format="JPEG")
    return base64.b64encode(buffered.getvalue()).decode()

async def verify():
    print("--- Verifying VisionGuide Integration ---")
    
    # 1. Check Vocabulary
    print("\n1. Checking Vocabulary...")
    print(f"Modes defined: {list(MODES.keys())}")
    assert 'E' in MODES
    assert 'S' in MODES
    assert 'M' in MODES
    print("Vocabulary check PASSED.")
    
    # 2. Check Engine Loading
    print("\n2. Checking YoloEngine...")
    engine = YoloEngine() # Will try to load YOLOv8m (might fail if not present but we check logic)
    print("Engine instantiated.")
    
    # 3. Check Mode Logic
    print("\n3. Checking Mode Logic...")
    # Since we can't easily query the model without downloading it, we will verify the code structure imports
    # by ensuring no import errors occurred above.
    import main
    print("Main module imported successfully.")
    
    print("\n--- Integration Verification Logic Complete ---")
    print("To fully verify, run the server and make a request.")

if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    loop.run_until_complete(verify())
