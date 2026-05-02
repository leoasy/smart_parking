from fastapi.testclient import TestClient
from typing import Any, Dict
import numpy as np
import cv2

from main_api import app
from app.deps import get_engine


class DummyEngine:
    def __init__(self):
        self.roi_map = {"roi_1": {1: object()}}

    def reset(self):
        return None

    def get_current_status(self):
        return {"snapshots": []}

    def process_image(self, image: np.ndarray, parking_lot_id: str, camera_id: int, visualize: bool = False) -> Dict[str, Any]:
        if parking_lot_id == "missing":
            raise ValueError("No ROI for parking_lot_id=missing")
        return {
            "parking_lot_id": parking_lot_id,
            "camera_id": camera_id,
            "camera_name": "cam-1",
            "slots": [{"slot_id": 1, "slot_code": "A-1", "occupied": True}],
            "events": [],
        }


def _dummy_jpg_bytes():
    image = np.zeros((10, 10, 3), dtype=np.uint8)
    ok, buffer = cv2.imencode(".jpg", image)
    assert ok
    return buffer.tobytes()


def test_health():
    app.dependency_overrides[get_engine] = lambda: DummyEngine()
    client = TestClient(app)
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"
    app.dependency_overrides.clear()

def test_status_endpoint():
    app.dependency_overrides[get_engine] = lambda: DummyEngine()
    client = TestClient(app)
    response = client.get("/parking/status")
    assert response.status_code == 200
    assert "snapshots" in response.json()
    app.dependency_overrides.clear()


def test_detect_image_success():
    app.dependency_overrides[get_engine] = lambda: DummyEngine()
    client = TestClient(app)

    response = client.post(
        "/parking/detect/image",
        data={"parking_lot_id": "roi_1", "camera_id": "1", "visualize": "false"},
        files={"file": ("a.jpg", _dummy_jpg_bytes(), "image/jpeg")},
    )

    assert response.status_code == 200
    payload = response.json()
    assert payload["parking_lot_id"] == "roi_1"
    assert payload["camera_id"] == 1
    assert payload["slots"][0]["occupied"] is True
    app.dependency_overrides.clear()


def test_detect_image_bad_roi_returns_400():
    app.dependency_overrides[get_engine] = lambda: DummyEngine()
    client = TestClient(app)

    response = client.post(
        "/parking/detect/image",
        data={"parking_lot_id": "missing", "camera_id": "1", "visualize": "false"},
        files={"file": ("a.jpg", _dummy_jpg_bytes(), "image/jpeg")},
    )

    assert response.status_code == 400
    app.dependency_overrides.clear()
