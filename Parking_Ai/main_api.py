# main_api.py
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.api import router
from app.config import HOST, PORT
from starlette.requests import Request
from starlette.responses import Response
import httpx
import time
import uuid
import logging

logger = logging.getLogger("parking_ai.api")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时创建 http_client
    from app.config import RUOYI_PUSH_TIMEOUT_SECONDS
    app.state.http_client = httpx.AsyncClient(
        timeout=RUOYI_PUSH_TIMEOUT_SECONDS,
        limits=httpx.Limits(max_connections=100, max_keepalive_connections=20)
    )
    yield
    # 关闭时清理 http_client
    await app.state.http_client.aclose()


app = FastAPI(
    title="Parking AI Service",
    version="1.0.0",
    lifespan=lifespan
)

app.include_router(router)


@app.middleware("http")
async def request_context_middleware(request: Request, call_next):
    request_id = request.headers.get("X-Request-ID", str(uuid.uuid4()))
    start = time.perf_counter()
    response: Response = await call_next(request)
    duration_ms = (time.perf_counter() - start) * 1000
    response.headers["X-Request-ID"] = request_id
    logger.info("request_id=%s method=%s path=%s status=%s duration_ms=%.2f",
                request_id, request.method, request.url.path, response.status_code, duration_ms)
    return response


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main_api:app",
        host=HOST,
        port=PORT,
        reload=True
    )
