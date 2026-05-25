import os

BACKEND_BASE_URL = os.getenv("BACKEND_BASE_URL", "http://127.0.0.1:8087").rstrip("/")
BACKEND_PARKING_API = os.getenv("BACKEND_PARKING_API", "/biz/parking/detect")
BACKEND_PUSH_TIMEOUT_SECONDS = float(os.getenv("BACKEND_PUSH_TIMEOUT_SECONDS", "3.0"))
BACKEND_PUSH_MAX_RETRIES = int(os.getenv("BACKEND_PUSH_MAX_RETRIES", "3"))
BACKEND_PUSH_BACKOFF_BASE = float(os.getenv("BACKEND_PUSH_BACKOFF_BASE", "0.5"))
HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", "8000"))
