# app/api.py
import cv2
import json
import numpy as np
import httpx
from fastapi import APIRouter, Depends, UploadFile, File, HTTPException,Form

from app.deps import get_engine
from app.schemas import DetectResponse, DetectImageRequest
from core.engine import ParkingEngine
from app.config import RUOYI_BASE_URL, RUOYI_PARKING_API

router = APIRouter()
http_client = httpx.AsyncClient(timeout=3.0)




@router.get("/health")
def health():
    return {"status": "ok"}


@router.get("/parking/rois")
def list_rois(engine: ParkingEngine = Depends(get_engine)):
    """
    查询当前加载的 ROI 结构
    """
    result = {}
    for pl_id, cams in engine.roi_map.items():
        result[pl_id] = list(cams.keys())
    return result


@router.post("/parking/reset")
def reset(engine: ParkingEngine = Depends(get_engine)):
    engine.reset()
    return {"status": "reset ok"}


@router.get("/parking/status")
def get_current_status(engine: ParkingEngine = Depends(get_engine)):
    """
    👉 若依可定时拉取的【当前稳定状态】
    """
    return engine.get_current_status()


@router.post(
    "/parking/detect/image",
    response_model=DetectResponse
)
async def detect_image(
    parking_lot_id: str = Form(...),
    camera_id: int = Form(...),
    visualize: bool = Form(False),
    file: UploadFile = File(...),
    engine: ParkingEngine = Depends(get_engine),
):

    """
    单张图片检测（支持状态变化推送若依）
    """

    # ---------- 1️⃣ 读取图片 ----------
    data = await file.read()
    img_array = np.frombuffer(data, np.uint8)
    image = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(status_code=400, detail="Invalid image")

    # ---------- 2️⃣ 调用引擎 ----------
    result = engine.process_image(
        image=image,
        parking_lot_id=parking_lot_id,
        camera_id=camera_id,
        visualize=visualize,
    )

    print(json.dumps(result, indent=2, ensure_ascii=False))

    # ---------- 3️⃣ 若有状态变化 → 推送若依 ----------
    if result.get("events"):
        payload = {
            "parking_lot_id": parking_lot_id,
            "camera_id": camera_id,
            "slots": result["slots"],
            "events": result["events"]
        }
        try:
            await http_client.post(
                RUOYI_BASE_URL + RUOYI_PARKING_API,
                json=payload
            )
        except Exception as e:
            print("⚠ 推送若依失败:", e)

    return result
