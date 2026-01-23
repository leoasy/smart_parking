# tests/test_roi_loader.py
import json
from pathlib import Path

from core.roi_loader import load_roi, load_roi_dir


def test_load_single_roi(tmp_path):
    """
    测试：加载单个 ROI JSON
    """
    roi_data = {
        "parking_lot_id": "roi_2",
        "camera_id": 1,
        "image_size": [1280, 720],
        "slots": [
            {
                "slot_id": 1,
                "camera_id": 1,
                "polygon": [
                    [458, 163],
                    [493, 155],
                    [474, 136]
                ]
            },
            {
                "slot_id": 2,
                "camera_id": 1,
                "polygon": [
                    [526, 158],
                    [487, 169],
                    [524, 201]
                ]
            }
        ]
    }

    roi_file = tmp_path / "roi_2_camera_1.json"
    roi_file.write_text(json.dumps(roi_data), encoding="utf-8")

    roi = load_roi(roi_file)

    assert roi.camera_id == 1
    assert roi.parking_lot_id == "roi_2"
    assert roi.image_size == (1280, 720)
    assert len(roi.slots) == 2
    assert roi.slots[0].slot_id == 1
    assert len(roi.slots[0].polygon) == 3


def test_load_roi_directory(tmp_path):
    """
    测试：加载 ROI 目录（多摄像头）
    """
    roi1 = {
        "parking_lot_id": "roi_1",
        "camera_id": 1,
        "image_size": [1280, 720],
        "slots": [
            {
                "slot_id": 1,
                "polygon": [[0, 0], [10, 0], [10, 10]]
            }
        ]
    }

    roi2 = {
        "parking_lot_id": "roi_2",
        "camera_id": 2,
        "image_size": [1920, 1080],
        "slots": [
            {
                "slot_id": 1,
                "polygon": [[20, 20], [30, 20], [30, 30]]
            }
        ]
    }

    (tmp_path / "roi_1_camera_1.json").write_text(
        json.dumps(roi1), encoding="utf-8"
    )
    (tmp_path / "roi_2_camera_2.json").write_text(
        json.dumps(roi2), encoding="utf-8"
    )

    roi_map = load_roi_dir(tmp_path)

    assert len(roi_map) == 2
    assert 1 in roi_map
    assert 2 in roi_map
    assert roi_map[2].image_size == (1920, 1080)

