# core/roi_loader.py
from dataclasses import dataclass
from pathlib import Path
from typing import List, Tuple, Dict, Optional
import json
import logging
import os

# =============================
# slot_code 生成规则（可统一改）
# =============================
# 例：
# parkingA-1
# parkingA-2
SLOT_CODE_FORMAT = os.getenv(
    "SLOT_CODE_FORMAT",
    "{parking_lot_id}-{slot_id}"
)


@dataclass
class SlotROI:
    """
    单个车位 ROI
    """
    slot_id: int                 # ROI 内编号（1,2,3…）
    camera_id: int
    polygon: List[Tuple[int, int]]
    slot_code: str               # 业务唯一标识（数据库用）


@dataclass
class CameraROI:
    """
    单个摄像头的所有 ROI
    """
    parking_lot_id: str
    camera_id: int
    image_size: Tuple[int, int]
    slots: List[SlotROI]


def load_roi(json_path: Path) -> CameraROI:
    """
    加载单个 ROI JSON 文件
    """
    json_path = Path(json_path)
    if not json_path.exists():
        raise FileNotFoundError(f"ROI file not found: {json_path}")

    try:
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid JSON in {json_path}: {e}")

    # ---------- 基本校验 ----------
    for k in ["parking_lot_id", "camera_id", "image_size", "slots"]:
        if k not in data:
            raise ValueError(f"Missing key '{k}' in {json_path}")

    parking_lot_id = str(data["parking_lot_id"])
    camera_id = int(data["camera_id"])

    w, h = data["image_size"]
    if not (isinstance(w, (int, float)) and isinstance(h, (int, float)) and w > 0 and h > 0):
        raise ValueError(f"Invalid image_size {data['image_size']} in {json_path}")
    image_size = (int(w), int(h))

    slots: List[SlotROI] = []

    for idx, slot in enumerate(data["slots"]):
        if "slot_id" not in slot or "polygon" not in slot:
            raise ValueError(f"Slot {idx} missing 'slot_id' or 'polygon' in {json_path}")

        slot_id = int(slot["slot_id"])
        slot_camera_id = int(slot.get("camera_id", camera_id))

        polygon = [(int(x), int(y)) for x, y in slot["polygon"]]

        # ====== polygon 坐标校验 ======
        if len(polygon) < 3:
            logger.warning(
                "Slot %d has invalid polygon (less than 3 points) in %s, skipping",
                slot_id, json_path
            )
            continue

        for pt_idx, (x, y) in enumerate(polygon):
            if x < 0 or y < 0:
                logger.warning(
                    "Slot %d has negative coordinate at point %d (%d,%d) in %s",
                    slot_id, pt_idx, x, y, json_path
                )
            if x > image_size[0] or y > image_size[1]:
                logger.warning(
                    "Slot %d coordinate at point %d (%d,%d) exceeds image size %s in %s",
                    slot_id, pt_idx, x, y, image_size, json_path
                )

        # ====== slot_code 处理 ======
        slot_code = slot.get("slot_code")
        if not slot_code:
            slot_code = SLOT_CODE_FORMAT.format(
                parking_lot_id=parking_lot_id,
                camera_id=slot_camera_id,
                slot_id=slot_id
            )

        slots.append(
            SlotROI(
                slot_id=slot_id,
                camera_id=slot_camera_id,
                polygon=polygon,
                slot_code=str(slot_code)
            )
        )

    # ====== 空 slots 检查 ======
    if not slots:
        logger.warning("No valid slots loaded from %s", json_path)

    return CameraROI(
        parking_lot_id=parking_lot_id,
        camera_id=camera_id,
        image_size=image_size,
        slots=slots
    )


def load_roi_dir_grouped(roi_dir: Path) -> Dict[str, Dict[int, CameraROI]]:
    """
    加载 ROI 目录（按停车场分组）

    返回：
    {
        "parkingA": {
            1: CameraROI
        }
    }
    """
    roi_dir = Path(roi_dir)
    if not roi_dir.exists():
        raise FileNotFoundError(f"ROI dir not found: {roi_dir}")

    grouped: Dict[str, Dict[int, CameraROI]] = {}

    json_files = list(roi_dir.glob("*.json"))
    if not json_files:
        logger.warning("No JSON files found in ROI directory: %s", roi_dir)
        return grouped

    for json_path in json_files:
        try:
            with open(json_path, "r", encoding="utf-8") as f:
                raw = json.load(f)
        except Exception as e:
            logger.warning("Failed to load %s: %s, skipping", json_path, e)
            continue

        try:
            camera_roi = load_roi(json_path)
        except Exception as e:
            logger.warning("Failed to parse ROI from %s: %s, skipping", json_path, e)
            continue

        parking_lot_id = camera_roi.parking_lot_id
        camera_id = camera_roi.camera_id

        if parking_lot_id not in grouped:
            grouped[parking_lot_id] = {}

        if camera_id in grouped[parking_lot_id]:
            logger.warning(
                "Duplicate camera_id=%d in parking_lot=%s (skipping second occurrence: %s)",
                camera_id, parking_lot_id, json_path
            )
            continue

        grouped[parking_lot_id][camera_id] = camera_roi

    return grouped
