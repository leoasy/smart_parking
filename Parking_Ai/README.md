# Parking AI 推理服务

本目录是 Smart Parking 的 AI 推理服务，基于 FastAPI 和 YOLO 模型，负责停车位占用检测、ROI 匹配、稳定性判断和检测结果回调后端。

## 技术栈

| 组件 | 说明 |
| --- | --- |
| FastAPI | HTTP API |
| Uvicorn | ASGI 服务 |
| Ultralytics / YOLO | 车辆检测 |
| OpenCV / NumPy | 图像处理 |
| httpx | 回调后端 |
| pytest | 单元测试 |

## 目录结构

```text
Parking_Ai/
├─ main_api.py          # FastAPI 入口
├─ core/                # 检测调度、ROI 加载、配置
├─ inference/           # YOLO 检测、ROI 匹配、稳定性判断
├─ app/                 # 应用辅助模块
├─ config/              # 配置
├─ data/                # 运行数据
├─ model/               # 本地模型目录，禁止提交
├─ tests/               # pytest 测试
├─ requirements.txt     # pip 依赖
└─ pyproject.toml       # 项目和测试配置
```

## WSL 环境

本项目此前通过 WSL 中的 hermes agent 修改，Python 环境也在 WSL。推荐继续使用：

```powershell
wsl bash -lc "cd /mnt/d/IDEA/smart-parking/Parking_Ai && /home/aoshiyue/venvs/smart-parking/bin/python -m pytest -q"
```

启动服务：

```powershell
wsl bash -lc "cd /mnt/d/IDEA/smart-parking/Parking_Ai && /home/aoshiyue/venvs/smart-parking/bin/python -m uvicorn main_api:app --host 0.0.0.0 --port 8000"
```

新建环境：

```bash
python -m venv /home/aoshiyue/venvs/smart-parking
/home/aoshiyue/venvs/smart-parking/bin/python -m pip install -r requirements.txt
```

## Docker

开发编排使用根目录 `docker-compose.yml` 中的 `parking-ai` 服务，构建文件为 `Dockerfile.python`：

```powershell
docker compose up -d parking-ai
```

镜像内服务监听 8000：

```text
http://localhost:8000/health
```

## 配置

可参考 `.env.example`：

```text
RUOYI_BASE_URL=http://127.0.0.1:8087
RUOYI_PARKING_API=/biz/parking/detect
RUOYI_PUSH_TIMEOUT_SECONDS=3.0
SLOT_CODE_FORMAT={parking_lot_id}-{slot_id}
```

Docker Compose 中默认回调后端：

```text
RUOYI_BASE_URL=http://ruoyi-server:8080
RUOYI_PARKING_API=/biz/parking/detect
```

## 模型文件

YOLO 模型放在 `model/` 目录，例如：

```text
Parking_Ai/model/parking.pt
```

模型文件禁止提交，`.gitignore` 已忽略 `.pt`、`.pth`、`.onnx` 等文件。

## 验证

```powershell
wsl bash -lc "cd /mnt/d/IDEA/smart-parking/Parking_Ai && /home/aoshiyue/venvs/smart-parking/bin/python -m pytest -q"
curl http://localhost:8000/health
```
