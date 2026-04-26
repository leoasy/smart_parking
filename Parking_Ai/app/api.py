# app/api.py
import logging
import numpy as np
import httpx
from fastapi import APIRouter, Depends, UploadFile, File, HTTPException, Form

from app.deps import get_engine
from app.schemas import DetectResponse
from core.engine import ParkingEngine
from app.config import RUOYI_BASE_URL, RUOYI_PARKING_API, RUOYI_PUSH_TIMEOUT_SECONDS

router = APIRouter()
logger = logging.getLogger(__name__)
http_client = httpx.AsyncClient(timeout=RUOYI_PUSH_TIMEOUT_SECONDS)



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
    若依定时拉取的当前稳定状态
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

    # ---------- 1.读取图片 ----------
    import cv2
    data = await file.read()
    img_array = np.frombuffer(data, np.uint8)
    image = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(status_code=400, detail="非法图片")

    # ---------- 2️.调用引擎 ----------
    try:
        result = engine.process_image(
            image=image,
            parking_lot_id=parking_lot_id,
            camera_id=camera_id,
            visualize=visualize,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except FileNotFoundError as exc:
        raise HTTPException(status_code=500, detail=f"模型或ROI文件缺失: {exc}") from exc
    except Exception as exc:
        logger.exception("检测失败 parking_lot_id=%s camera_id=%s", parking_lot_id, camera_id)
        raise HTTPException(status_code=500, detail="检测服务内部错误") from exc

    logger.info(
        "detect_image success parking_lot_id=%s camera_id=%s slots=%d",
        parking_lot_id,
        camera_id,
        len(result.get("slots", [])),
    )

    # ---------- 3️.推送若依 ----------
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
        except httpx.HTTPError as e:
            logger.warning(
                "推送若依失败 parking_lot_id=%s camera_id=%s err=%s",
                parking_lot_id,
                camera_id,
                str(e),
            )

    return result
