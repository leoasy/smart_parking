# utils/draw.py
from typing import List, Dict
import cv2
import numpy as np

from core.roi_loader import SlotROI
from inference.roi_matcher import Detection


# ---------- 颜色定义 ----------
COLOR_OCCUPIED = (0, 0, 255)    # 红色
COLOR_FREE = (0, 255, 0)        # 绿色
COLOR_DET = (255, 0, 0)         # 蓝色
COLOR_TEXT = (255, 255, 255)    # 白色


def draw_slots(
    image: np.ndarray,
    slots: List[SlotROI],
    states: Dict[int, bool],
    alpha: float = 0.4,
):
    """
    在图像上绘制车位 ROI 及占用状态

    :param image: BGR image (will be modified in-place)
    :param slots: List[SlotROI]
    :param states: {slot_id: occupied}
    :param alpha: 透明度
    """
    overlay = image.copy()

    for slot in slots:
        polygon = np.array(slot.polygon, dtype=np.int32)
        occupied = states.get(slot.slot_id, False)

        color = COLOR_OCCUPIED if occupied else COLOR_FREE

        # 填充 polygon
        cv2.fillPoly(overlay, [polygon], color)

        # 画边框
        cv2.polylines(image, [polygon], isClosed=True, color=color, thickness=2)

        # 写 slot_id
        cx = int(polygon[:, 0].mean())
        cy = int(polygon[:, 1].mean())
        cv2.putText(
            image,
            f"ID {slot.slot_id}",
            (cx - 15, cy),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.5,
            COLOR_TEXT,
            1,
            cv2.LINE_AA,
        )

    # 透明叠加
    cv2.addWeighted(overlay, alpha, image, 1 - alpha, 0, image)


def draw_detections(
    image: np.ndarray,
    detections: List[Detection],
):
    """
    在图像上绘制检测框

    :param image: BGR image (will be modified in-place)
    :param detections: List[Detection]
    """
    for det in detections:
        x1, y1, x2, y2 = det.bbox

        # bbox
        cv2.rectangle(
            image,
            (x1, y1),
            (x2, y2),
            COLOR_DET,
            2,
        )

        # score
        label = f"{det.score:.2f}"
        cv2.putText(
            image,
            label,
            (x1, max(y1 - 5, 15)),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.5,
            COLOR_DET,
            1,
            cv2.LINE_AA,
        )
