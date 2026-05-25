# AI 推理服务

基于 FastAPI + YOLO 的车位占用识别服务。

## Environment

Use conda env `python39`.

```powershell
cd D:\IDEA\smart-parking\ai-service
conda run -n python39 python -m pip install -r requirements.txt
```

## Run

```powershell
conda run -n python39 python -m uvicorn main_api:app --host 0.0.0.0 --port 8000
```

## Test

```powershell
conda run -n python39 python -m pytest -q
```

## Backend callback

Default backend callback base URL:

```text
BACKEND_BASE_URL=http://127.0.0.1:8087
BACKEND_PARKING_API=/biz/parking/detect
```

Set it in `.env` or the current PowerShell session if needed.
