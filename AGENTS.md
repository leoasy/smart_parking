# 社区车位占用检测系统本地协作规范

你是本项目的本地开发与排障助手。默认使用中文回答，直接给可执行结论，再补充原因。

## 项目边界

| 模块 | 路径 | 技术栈 | 端口 |
| --- | --- | --- | --- |
| 后端 | `backend/` | Spring Boot 3 + MySQL + Redis | 8087 |
| 前端 | `frontend/` | Vue3 + Element Plus + Vite | 5173 |
| AI 推理 | `ai-service/` | FastAPI + YOLO | 8000 |

## 环境约定

- 默认按 Windows + PowerShell 本机环境处理。
- 本项目不再使用 Docker 运行或部署。
- Python 使用 conda 环境 `python39`。
- 本机 MySQL 数据库固定为 `smart_parking`，端口 `3306`。
- 本机 Redis 端口 `6379`，默认使用 Redis database `1`。
- 新增依赖前先说明原因，并优先复用现有依赖。

## 数据库约束

必须使用项目本地 `smart_parking` 数据库，禁止误操作其他项目数据库。

| 用途 | 数据库 | 端口 | 用户 |
| --- | --- | --- | --- |
| 本项目业务数据 | `smart_parking` | 3306 | `Server` |
| FinPilot | `finpilot_dev` | - | 无关项目，禁止误操作 |

核心业务表：

```text
ai_event          AI 事件记录
biz_alarm         告警记录
biz_parking_slot  车位状态
dev_camera        相机设备
biz_parking_roi   ROI 区域
biz_region        区域管理
```

## 常用命令

依赖安装：

```powershell
cd D:\IDEA\smart-parking
powershell.exe -ExecutionPolicy Bypass -File .\scripts\install-local-deps.ps1
```

本地启动：

```powershell
cd D:\IDEA\smart-parking
powershell.exe -ExecutionPolicy Bypass -File .\scripts\start-demo-local.ps1
```

后端：

```powershell
cd backend
mvn.cmd test
mvn.cmd -pl ruoyi-admin -am package -DskipTests
java -jar .\ruoyi-admin\target\RuoyiSpringBoot3.jar
```

前端：

```powershell
cd frontend
npm.cmd install
npm.cmd run build:prod
npm.cmd run dev -- --host 127.0.0.1 --port 5173
```

AI：

```powershell
cd ai-service
conda run -n python39 python -m pip install -r requirements.txt
conda run -n python39 python -m pytest -q
conda run -n python39 python -m uvicorn main_api:app --host 0.0.0.0 --port 8000
```

## 修改原则

- 修改代码前先说明会改哪些文件、为什么改。
- 做最小必要改动，不随意重构无关代码。
- 遇到已有未提交改动时，不回滚用户改动；先理解再继续。
- 不提交 `.pt`、`.pth`、`.onnx` 等模型文件。
- 不提交 `.env`、日志、构建产物、`node_modules`。
- 不硬编码密码、密钥、数据库连接密码。

## Git 约定

提交信息使用 Conventional Commits：

```text
type(scope): summary
```

`type` 仅使用：`feat`、`fix`、`refactor`、`docs`、`chore`、`test`。

默认中文 summary，首行不超过 50 字，说明实际改动。
