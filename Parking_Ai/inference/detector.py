# inference/detector.py
from typing import List, Tuple
import torch
import numpy as np

from ultralytics import YOLO

from inference.roi_matcher import Detection


class YOLODetector:
    """
    YOLO 检测器（支持 CPU / GPU）
    """

    def __init__(
        self,
        model_path: str,
        img_size: Tuple[int, int] = (640, 384),
        conf_thres: float = 0.25,
        iou_thres: float = 0.45,
        device: str = None,
    ):
        """
        :param model_path: YOLO 权重路径
        :param img_size: (width, height)
        :param conf_thres: 置信度阈值
        :param iou_thres: NMS IOU 阈值
        :param device: "cuda" / "cpu" / None（自动）
        """
        self.model_path = model_path
        self.img_size = img_size
        self.conf_thres = conf_thres
        self.iou_thres = iou_thres

        # -------- device --------
        if device is None:
            self.device = "cuda" if torch.cuda.is_available() else "cpu"
        else:
            self.device = device

        # -------- load model --------
        self.model = YOLO(self.model_path)
        self.model.to(self.device)

    def detect(self, image: np.ndarray) -> List[Detection]:
        """
        对单帧图像进行检测

        :param image: np.ndarray (BGR)
        :return: List[Detection]
        """
        if image is None:
            return []

        # Ultralytics 支持直接传 BGR numpy
        results = self.model.predict(
            source=image,
            imgsz=self.img_size,
            conf=self.conf_thres,
            iou=self.iou_thres,
            device=self.device,
            verbose=False,
        )

        detections: List[Detection] = []

        if not results:
            return detections

        result = results[0]
        if result.boxes is None:
            return detections

        boxes = result.boxes

        for box in boxes:
            # xyxy: (x1, y1, x2, y2)
            x1, y1, x2, y2 = box.xyxy[0].tolist()
            score = float(box.conf[0])
            cls_id = int(box.cls[0])

            detections.append(
                Detection(
                    bbox=(
                        int(x1),
                        int(y1),
                        int(x2),
                        int(y2),
                    ),
                    score=score,
                    cls=cls_id,
                )
            )

        return detections
