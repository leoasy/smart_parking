# tests/test_roi_matcher.py
from inference.roi_matcher import ROIMatcher, Detection
from core.roi_loader import CameraROI, SlotROI


def test_center_match():
    roi = CameraROI(
        parking_lot_id="roi_test",
        camera_id=1,
        image_size=(640, 480),
        slots=[
            SlotROI(
                slot_id=1,
                camera_id=1,
                polygon=[(0, 0), (100, 0), (100, 100), (0, 100)]
            )
        ]
    )

    detections = [
        Detection(bbox=(10, 10, 50, 50), score=0.9),
        Detection(bbox=(200, 200, 300, 300), score=0.8),
    ]

    matcher = ROIMatcher(method="center")
    result = matcher.match(detections, roi)

    assert result[1] == 0  # 第 0 个 detection 占用 slot 1
