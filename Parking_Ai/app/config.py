# app/config.py
import os

RUOYI_BASE_URL = os.getenv("RUOYI_BASE_URL", "http://127.0.0.1:8087").rstrip("/")
RUOYI_PARKING_API = os.getenv("RUOYI_PARKING_API", "/biz/parking/detect")
RUOYI_PUSH_TIMEOUT_SECONDS = float(os.getenv("RUOYI_PUSH_TIMEOUT_SECONDS", "3.0"))
RUOYI_PUSH_MAX_RETRIES = int(os.getenv("RUOYI_PUSH_MAX_RETRIES", "3"))
RUOYI_PUSH_BACKOFF_BASE = float(os.getenv("RUOYI_PUSH_BACKOFF_BASE", "0.5"))
HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", "8000"))
