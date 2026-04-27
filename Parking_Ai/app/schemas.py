# app/schemas.py
from typing import List, Optional
from pydantic import BaseModel, Field
from datetime import datetime


# ============ 基础响应 ============
class SlotResult(BaseModel):
    slot_id: int
    slot_code: str
    occupied: bool


class DetectResponse(BaseModel):
    parking_lot_id: str
    camera_id: int
    camera_name: str
    slots: List[SlotResult]
    events: Optional[List[dict]] = None


class DetectImageRequest(BaseModel):
    parking_lot_id: str
    camera_id: int
    visualize: bool = False


# ============ 指标相关 ============
class HealthResponse(BaseModel):
    status: str
    parking_lots_loaded: int = 0


class ReadyResponse(BaseModel):
    status: str
    parking_lots_loaded: int


class ROIResponse(BaseModel):
    """ROI列表响应"""
    parking_lot_id: str
    camera_ids: List[int]


class ResetResponse(BaseModel):
    status: str = "reset ok"


class ParkingStatusResponse(BaseModel):
    """当前车场状态"""
    parking_lot_id: str
    timestamp: Optional[datetime] = None
    cameras: List["CameraStatus"] = []


class CameraStatus(BaseModel):
    camera_id: int
    total_slots: int = 0
    occupied_slots: int = 0
    free_slots: int = 0


# ============ 错误响应 ============
class ErrorResponse(BaseModel):
    detail: str
    error_code: Optional[str] = None


class HTTPValidationError(BaseModel):
    detail: List["ValidationError"]


class ValidationError(BaseModel):
    loc: List[str]
    msg: str
    type: str
