import logging
import asyncio
import cv2
import numpy as np
import httpx
from fastapi import APIRouter, Depends, UploadFile, File, HTTPException, Form, Request

from app.deps import get_engine
from app.schemas import DetectResponse
from core.engine import ParkingEngine
from app.config import (
    BACKEND_BASE_URL,
    BACKEND_PARKING_API,
    BACKEND_PUSH_TIMEOUT_SECONDS,
    BACKEND_PUSH_MAX_RETRIES,
    BACKEND_PUSH_BACKOFF_BASE,
)

router = APIRouter()
logger = logging.getLogger(__name__)


class PushError(Exception):
    def __init__(self, parking_lot_id: str, camera_id: int, cause: str):
        self.parking_lot_id = parking_lot_id
        self.camera_id = camera_id
        self.cause = cause
        super().__init__(f"推送后端失败 parking_lot_id={parking_lot_id} camera_id={camera_id} cause={cause}")


@router.get("/health")
async def health(engine: ParkingEngine = Depends(get_engine)):
    """
    健康检查：返回服务状态和引擎已加载的车场数量
    """
    roi_map = engine.roi_map
    return {
        "status": "ok",
        "parking_lots_loaded": len(roi_map),
    }

@router.get("/health/ready")
async def health_ready(engine: ParkingEngine = Depends(get_engine)):
    """
    readinessProbe 专用：确认引擎已初始化且 ROI 已加载
    """
    roi_map = engine.roi_map
    if not roi_map:
        raise HTTPException(status_code=503, detail="Engine not initialized: no ROI loaded")
    return {"status": "ready", "parking_lots_loaded": len(roi_map)}


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
    return engine.get_current_status()


def _build_push_payload(parking_lot_id: str, camera_id: int, result: dict) -> dict:
    return {
        "parking_lot_id": parking_lot_id,
        "camera_id": camera_id,
        "slots": result["slots"],
        "events": result["events"]
    }


async def _push_to_backend(http_client: httpx.AsyncClient, push_url: str, payload: dict, parking_lot_id: str, camera_id: int) -> None:
    max_retries = int(BACKEND_PUSH_MAX_RETRIES)
    backoff_base = float(BACKEND_PUSH_BACKOFF_BASE)

    for attempt in range(max_retries):
        delay = backoff_base * (2 ** attempt)
        try:
            response = await http_client.post(push_url, json=payload, timeout=BACKEND_PUSH_TIMEOUT_SECONDS)
            if response.status_code >= 200 and response.status_code < 300:
                return
            cause = f"HTTP {response.status_code}"
        except httpx.TimeoutException:
            cause = "timeout"
        except httpx.HTTPError as e:
            cause = str(e)

        if attempt < max_retries - 1:
            logger.warning(
                "推送后端失败(第%d次) parking_lot_id=%s camera_id=%s err=%s, %.1fs后重试",
                attempt + 1, parking_lot_id, camera_id, cause, delay
            )
            await asyncio.sleep(delay)
        else:
            raise PushError(parking_lot_id=parking_lot_id, camera_id=camera_id, cause=cause)


@router.post(
    "/parking/detect/image",
    response_model=DetectResponse
)
async def detect_image(
    request: Request,
    parking_lot_id: str = Form(...),
    camera_id: int = Form(...),
    visualize: bool = Form(True),
    file: UploadFile = File(...),
    engine: ParkingEngine = Depends(get_engine),
):
    """
    单张图片检测，出现状态变化时推送后端。
    """

    # ---------- 1.读取图片 ----------
    data = await file.read()
    img_array = np.frombuffer(data, np.uint8)
    image = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(status_code=400, detail="非法图片")

    # ---------- 2.调用引擎 ----------
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

    if result.get("events"):
        http_client = request.app.state.http_client
        payload = _build_push_payload(parking_lot_id, camera_id, result)
        push_url = BACKEND_BASE_URL + BACKEND_PARKING_API
        try:
            await _push_to_backend(http_client, push_url, payload, parking_lot_id, camera_id)
        except PushError as e:
            logger.error(str(e))
            raise HTTPException(status_code=502, detail=f"推送后端失败: {e.cause}") from e

    return result
