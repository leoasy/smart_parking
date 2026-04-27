# app/schemas.py
from typing import List, Optional
from pydantic import BaseModel


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
