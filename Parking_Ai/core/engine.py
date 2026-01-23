# core/engine.py
from typing import Dict, Any, List
from pathlib import Path
import cv2

from core.config import Config, PARKING_LOT_CODE_MAP, CAMERA_NAME_MAP
from core.roi_loader import CameraROI, load_roi_dir_grouped
from inference.roi_matcher import ROIMatcher, Detection
from inference.stability import SlotStability
from utils.draw import draw_slots, draw_detections


class ParkingEngine:
    """
    停车位检测核心引擎
    支持：多停车场 + 多摄像头
    """

    def __init__(self, config: Config):
        self.cfg = config

        # ROI: parking_lot_id -> camera_id -> CameraROI
        self.roi_map: Dict[str, Dict[int, CameraROI]] = load_roi_dir_grouped(
            Path("data/roi")
        )

        self.matcher = ROIMatcher(
            method=self.cfg.matcher.method,
            iou_threshold=self.cfg.matcher.iou_threshold,
            center_margin=self.cfg.matcher.center_margin,
        )

        self.stability = SlotStability(win=self.cfg.stability.win)
        self.detector = None

    # ===============================
    # Public API
    # ===============================
    def process_image(
        self,
        image: Any,
        parking_lot_id: str,
        camera_id: int,
        visualize: bool = False,
    ) -> Dict[str, Any]:

        if parking_lot_id not in self.roi_map:
            raise ValueError(f"No ROI for parking_lot_id={parking_lot_id}")

        if camera_id not in self.roi_map[parking_lot_id]:
            raise ValueError(
                f"No ROI for camera_id={camera_id} "
                f"in parking_lot_id={parking_lot_id}"
            )

        camera_roi = self.roi_map[parking_lot_id][camera_id]

        # ====== 建立索引映射 ======
        slot_map = {
            slot.slot_id: slot
            for slot in camera_roi.slots
        }

        # 1️⃣ 检测
        detections = self._detect(image)

        # 2️⃣ ROI 匹配（返回 index -> detection_index）
        match_result = self.matcher.match(
            detections=detections,
            camera_roi=camera_roi
        )

        # index -> occupied
        current_state = {
            slot_id: (det_idx is not None)
            for slot_id, det_idx in match_result.items()
        }

        # 3️⃣ 稳定状态
        stable_state = self.stability.update(current_state)

        # 4️⃣ 可视化
        if visualize and self.cfg.visualize.enable:
            vis_img = image.copy()
            draw_slots(vis_img, camera_roi.slots, stable_state)
            draw_detections(vis_img, detections)
            self._save_visualization(vis_img)

        # ===============================
        # 返回结构（核心）
        # ===============================
        slots_result = []

        parking_lot_code = PARKING_LOT_CODE_MAP.get(
            parking_lot_id,
            parking_lot_id  # fallback，防止没配置直接炸
        )

        camera_name = (
            CAMERA_NAME_MAP
            .get(parking_lot_id, {})
            .get(camera_id, f"{parking_lot_id}-摄像头{camera_id}")
        )

        for slot_id, occupied in stable_state.items():
            slot = slot_map.get(slot_id)
            if not slot:
                continue  # 防御式，理论上不会发生

            slots_result.append({
                "slot_id": slot.slot_id,
                "slot_code": f"{parking_lot_code}-{slot.slot_id}",
                "occupied": occupied
            })

        return {
            "parking_lot_id": parking_lot_id,
            "camera_id": camera_id,
            "camera_name": camera_name,
            "slots": slots_result,
            "events": []  # 事件建议由 Spring 侧生成
        }

    def reset(self):
        self.stability.reset()

    # ===============================
    # Internal
    # ===============================
    def _detect(self, image) -> List[Detection]:
        if self.detector is None:
            self.detector = self._load_detector()
        return self.detector.detect(image)

    def _load_detector(self):
        from inference.detector import YOLODetector
        return YOLODetector(
            model_path=self.cfg.model.path,
            img_size=self.cfg.detector.img_size,
            conf_thres=self.cfg.detector.conf_threshold,
            iou_thres=self.cfg.detector.iou_threshold,
        )

    def _save_visualization(self, image):
        out_dir = Path(self.cfg.visualize.save_dir)
        out_dir.mkdir(parents=True, exist_ok=True)
        idx = len(list(out_dir.glob("*.jpg"))) + 1
        cv2.imwrite(str(out_dir / f"result_{idx}.jpg"), image)
