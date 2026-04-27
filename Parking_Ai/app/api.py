# app/api.py
import logging
import asyncio
import cv2
import numpy as np
import httpx
from fastapi import APIRouter, Depends, UploadFile, File, HTTPException, Form, Request

from app.deps import get_engine
from app.schemas import DetectResponse
from core.engine import ParkingEngine
from app.config import RUOYI_BASE_URL, RUOYI_PUSH_TIMEOUT_SECONDS

router = APIRouter()
logger = logging.getLogger(__name__)


class PushError(Exception):
    """推送若依失败异常"""
    def __init__(self, parking_lot_id: str, camera_id: int, cause: str):
        self.parking_lot_id = parking_lot_id
        self.camera_id = camera_id
        self.cause = cause
        super().__init__(f"推送若依失败 parking_lot_id={parking_lot_id} camera_id={camera_id} cause={cause}")


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


def _build_push_payload(parking_lot_id: str, camera_id: int, result: dict) -> dict:
    return {
        "parking_lot_id": parking_lot_id,
        "camera_id": camera_id,
        "slots": result["slots"],
        "events": result["events"]
    }


async def _push_to_ruoyi(http_client: httpx.AsyncClient, push_url: str, payload: dict, parking_lot_id: str, camera_id: int) -> None:
    """
    使用指数退避向若依推送数据，只接受 2xx 响应，重试耗尽后抛出 PushError
    """
    backoff = [0.5, 1.0, 2.0]  # 指数退避时间序列

    for attempt, delay in enumerate(backoff):
        try:
            response = await http_client.post(push_url, json=payload)
            if response.status_code >= 200 and response.status_code < 300:
                return  # 成功
            # 非 2xx 视为失败
            cause = f"HTTP {response.status_code}"
        except httpx.HTTPError as e:
            cause = str(e)

        if attempt < len(backoff) - 1:
            logger.warning(
                "推送若依失败(第%d次) parking_lot_id=%s camera_id=%s err=%s, %.1fs后重试",
                attempt + 1, parking_lot_id, camera_id, cause, delay
            )
            await asyncio.sleep(delay)
        else:
            # 重试耗尽，抛出异常
            raise PushError(parking_lot_id=parking_lot_id, camera_id=camera_id, cause=cause)


@router.post(
    "/parking/detect/image",
    response_model=DetectResponse
)
async def detect_image(
    request: Request,
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

    # ---------- 3️.推送若依 ----------
    if result.get("events"):
        http_client = request.app.state.http_client
        payload = _build_push_payload(parking_lot_id, camera_id, result)
        push_url = RUOYI_BASE_URL + "/biz/parking/detect"
        try:
            await _push_to_ruoyi(http_client, push_url, payload, parking_lot_id, camera_id)
        except PushError as e:
            logger.error(str(e))
            raise HTTPException(status_code=502, detail=f"推送若依失败: {e.cause}") from e

    return result
