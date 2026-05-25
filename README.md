# 社区车位占用检测系统

社区车位占用检测系统，包含本地后端、前端管理端和 AI 推理服务。项目默认在 Windows 本机运行，不依赖 Docker。

## 目录结构

| 目录 | 说明 | 默认端口 |
| --- | --- | --- |
| `backend/` | Spring Boot 3 后端服务 | 8087 |
| `frontend/` | Vue3 + Element Plus 前端 | 5173 |
| `ai-service/` | FastAPI + YOLO 推理服务 | 8000 |
| `Redis/` | Windows 便携 Redis 运行组件 | 6379 |
| `scripts/` | 本地安装、启动、数据库导入脚本 | - |
| `docs/` | 使用说明和数据库说明 | - |

## 快速启动

```powershell
cd D:\IDEA\smart-parking
Copy-Item .env.example .env
Start-Process powershell.exe -ArgumentList '-NoExit', '-Command', 'cd D:\IDEA\smart-parking\Redis; .\redis-server.exe .\redis.conf'
powershell.exe -ExecutionPolicy Bypass -File .\scripts\install-local-deps.ps1
powershell.exe -ExecutionPolicy Bypass -File .\scripts\start-demo-local.ps1
```

访问入口：

```text
前端：http://127.0.0.1:5173/admin/
后端：http://127.0.0.1:8087
AI：http://127.0.0.1:8000
```

## 数据库

本项目使用本机 MySQL 数据库 `smart_parking`。首次演示前导入根目录的 `smart_parking.sql`：

```powershell
powershell.exe -ExecutionPolicy Bypass -File .\scripts\import-smart-parking-db.ps1 `
  -User Server `
  -Password "你的MySQL密码"
```

## 文档

- [使用说明书](docs/使用说明书.md)
- [数据库说明](docs/数据库说明.md)
- [二次开发说明](docs/二次开发说明.md)
- [提交包说明](docs/提交包说明.md)
- [开源来源说明](NOTICE.md)

## 注意

- Python 使用 conda 环境 `python39`。
- 本项目不再使用 Docker 运行或部署。
- 离线提交包包含 `Redis/` 和 `ai-service/model/parking.pt`，可用于恢复运行环境。
- Git 仓库不提交 `.env`、模型文件、Redis 运行数据、日志、构建产物和 AI 输出图片。
