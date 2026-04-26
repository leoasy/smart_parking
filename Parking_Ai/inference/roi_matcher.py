# inference/roi_matcher.py
from typing import List, Dict, Tuple, Optional
from dataclasses import dataclass

from core.roi_loader import CameraROI, SlotROI
from utils.geometry import point_in_polygon, polygon_bbox, bbox_iou


@dataclass
class Detection:
    """
    单个检测结果（统一格式）
    """
    bbox: Tuple[int, int, int, int]  # (x1, y1, x2, y2)
    score: float
    cls: int = 0  # 车辆类别（可扩展）


class ROIMatcher:
    """
    将检测结果匹配到 ROI（停车位）
    """

    def __init__(
        self,
        method: str = "center",
        iou_threshold: float = 0.2,
        center_margin: float = 0.5,
    ):
        if method not in ("center", "iou"):
            raise ValueError("method must be 'center' or 'iou'")
        self.method = method
        self.iou_threshold = iou_threshold
        self.center_margin = center_margin

    def match(
        self,
        detections: List[Detection],
        camera_roi: CameraROI,
    ) -> Dict[int, Optional[int]]:
        """
        匹配检测结果到车位

        :return:
            {slot_id: detection_index or None}
        """
        results: Dict[int, Optional[int]] = {
            slot.slot_id: None for slot in camera_roi.slots
        }

        for det_idx, det in enumerate(detections):
            if self.method == "center":
                matched_slot = self._match_center(det, camera_roi)
            else:
                matched_slot = self._match_iou(det, camera_roi)

            if matched_slot is not None:
                # 一个 slot 只认第一个匹配到的车
                if results[matched_slot] is None:
                    results[matched_slot] = det_idx

        return results

    # ---------------- private ---------------- #

    def _match_center(
        self,
        det: Detection,
        camera_roi: CameraROI,
    ) -> Optional[int]:
        """
        使用 bbox 中心点进行匹配
        """
        x1, y1, x2, y2 = det.bbox
        cx = int((x1 + x2) / 2)
        cy = int((y1 + y2) / 2)

        for slot in camera_roi.slots:
            if point_in_polygon((cx, cy), slot.polygon):
                return slot.slot_id
        return None

    def _match_iou(
        self,
        det: Detection,
        camera_roi: CameraROI,
    ) -> Optional[int]:
        """
        IoU 匹配：与 ROI 外接框 IoU 最高且超过阈值的 slot
        """
        best_slot_id: Optional[int] = None
        best_iou = 0.0

        for slot in camera_roi.slots:
            slot_bbox = polygon_bbox(slot.polygon)
            iou = bbox_iou(det.bbox, slot_bbox)
            if iou > best_iou:
                best_iou = iou
                best_slot_id = slot.slot_id

        if best_slot_id is not None and best_iou >= self.iou_threshold:
            return best_slot_id
        return None
