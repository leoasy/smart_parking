# core/roi_loader.py
from dataclasses import dataclass
from pathlib import Path
from typing import List, Tuple, Dict, Optional
import json
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

    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    # ---------- 基本校验 ----------
    for k in ["parking_lot_id", "camera_id", "image_size", "slots"]:
        if k not in data:
            raise ValueError(f"Missing key '{k}' in {json_path}")

    parking_lot_id = str(data["parking_lot_id"])
    camera_id = int(data["camera_id"])

    w, h = data["image_size"]
    image_size = (int(w), int(h))

    slots: List[SlotROI] = []

    for slot in data["slots"]:
        if "slot_id" not in slot or "polygon" not in slot:
            raise ValueError("Each slot must contain slot_id and polygon")

        slot_id = int(slot["slot_id"])
        slot_camera_id = int(slot.get("camera_id", camera_id))

        polygon = [(int(x), int(y)) for x, y in slot["polygon"]]

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

    for json_path in roi_dir.glob("*.json"):
        with open(json_path, "r", encoding="utf-8") as f:
            raw = json.load(f)

        parking_lot_id = str(raw["parking_lot_id"])
        camera_id = int(raw["camera_id"])

        camera_roi = load_roi(json_path)

        grouped.setdefault(parking_lot_id, {})
        if camera_id in grouped[parking_lot_id]:
            raise ValueError(
                f"Duplicate camera_id={camera_id} in parking_lot={parking_lot_id}"
            )

        grouped[parking_lot_id][camera_id] = camera_roi

    return grouped
