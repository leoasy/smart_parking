# core/engine.py
from typing import Dict, Any, List, Tuple
from pathlib import Path
import cv2
from datetime import datetime
import hashlib
import numpy as np

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
            self.cfg.roi_dir
        )

        self.matcher = ROIMatcher(
            method=self.cfg.matcher.method,
            iou_threshold=self.cfg.matcher.iou_threshold,
            center_margin=self.cfg.matcher.center_margin,
        )

        self.stability = SlotStability(win=self.cfg.stability.win)
        self.detector = None
        self._latest_status: Dict[Tuple[str, int], Dict[int, bool]] = {}

    # ===============================
    # Public API
    # ===============================
    def process_image(
        self,
        image: np.ndarray,
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

        # 使用 scoped key，避免多摄像头 slot_id 冲突
        scoped_current_state = {
            self._state_key(parking_lot_id, camera_id, slot_id): (det_idx is not None)
            for slot_id, det_idx in match_result.items()
        }

        # 3️⃣ 稳定状态
        stable_state_scoped = self.stability.update(scoped_current_state)
        stable_state = {
            slot_id: stable_state_scoped[self._state_key(parking_lot_id, camera_id, slot_id)]
            for slot_id in match_result.keys()
        }
        self._latest_status[(parking_lot_id, camera_id)] = dict(stable_state)

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
        self._latest_status.clear()

    def get_current_status(self) -> Dict[str, Any]:
        snapshots = []
        for (parking_lot_id, camera_id), states in self._latest_status.items():
            snapshots.append(
                {
                    "parking_lot_id": parking_lot_id,
                    "camera_id": camera_id,
                    "slots": [
                        {"slot_id": slot_id, "occupied": occupied}
                        for slot_id, occupied in sorted(states.items())
                    ],
                }
            )
        return {"snapshots": snapshots}

    # ===============================
    # Internal
    # ===============================
    def _detect(self, image: np.ndarray) -> List[Detection]:
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

    def _save_visualization(self, image: np.ndarray) -> None:
        out_dir = Path(self.cfg.visualize.save_dir)
        out_dir.mkdir(parents=True, exist_ok=True)
        ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
        cv2.imwrite(str(out_dir / f"result_{ts}.jpg"), image)

    @staticmethod
    def _state_key(parking_lot_id: str, camera_id: int, slot_id: int) -> int:
        # 为了兼容 SlotStability 仅接受 int key，这里做稳定哈希映射
        raw = f"{parking_lot_id}:{camera_id}:{slot_id}"
        digest = hashlib.sha1(raw.encode("utf-8")).hexdigest()[:12]
        return int(digest, 16)
