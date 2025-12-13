import requests
import base64
import json

def test_accessibility():
    # Load test image
    with open("test_desk_with_pen.png", "rb") as f:
        image_bytes = f.read()
    image_b64 = base64.b64encode(image_bytes).decode("utf-8")

    # Test /detect with spatial context
    print("\nTesting /detect (Spatial Context)...")
    try:
        response = requests.post(
            "http://localhost:8000/detect",
            json={"image": image_b64}
        )
        if response.status_code == 200:
            data = response.json()
            print(f"Total Detections: {data['total_detections']}")
            for d in data['detections']:
                print(f"- {d['label']}: {d['score']:.2f} | Pos: {d['position_desc']} | Prox: {d['proximity_desc']}")
        else:
            print(f"Error: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Connection Error: {e}")

    # Test /describe_scene
    print("\nTesting /describe_scene...")
    try:
        response = requests.post(
            "http://localhost:8000/describe_scene",
            json={"image": image_b64}
        )
        if response.status_code == 200:
            data = response.json()
            print("Scene Description:")
            print(data['description'])
        else:
            print(f"Error: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Connection Error: {e}")

if __name__ == "__main__":
    test_accessibility()
