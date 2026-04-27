# Smart Parking 项目规范

## 技术栈

| 服务 | 技术栈 | 端口 |
|------|--------|------|
| 后端 | RuoYi SpringBoot3 + MySQL + Redis | 8087 |
| 前端 | Vue3 + ElementPlus + Vite | 80 |
| AI | Python FastAPI + YOLO | 8000 |
| AI 网关 | new-api | 3000 |

## 数据库

**必须使用 `mysql-smartparking` MCP**，禁止使用 `mysql` MCP。

所有数据库操作均通过 `mcp_mysql_smartparking_*` 调用。

| 用途 | 数据库 | 端口 | 用户 | 密码 |
|------|--------|------|------|------|
| 本项目业务数据 | `smart_parking` | 3306 | `smart_parking` | `sp_2024` |
| FinPilot（无关） | `finpilot_dev` | — | — | — |

系统登录：admin / 12345678

## 核心业务表

```
ai_event          — AI 事件记录
biz_alarm         — 告警记录
biz_parking_slot  — 车位状态
dev_camera        — 相机设备
biz_parking_roi  — ROI 区域
biz_region        — 区域管理
```

## 启动方式

```powershell
# Docker 启动所有服务
docker compose up -d

# 验证健康状态
curl http://localhost:80/health       # 前端
curl http://localhost:8087/actuator/health  # 后端
curl http://localhost:8000/health    # AI
```

## 子模块规范

### 后端（RuoYi-SpringBoot3-Pro-master/）

- Spring Boot 3 + MySQL + Redis + Kafka
- 编码规范见 `.cursorrules`
- 构建：`mvn clean package -DskipTests`
- Docker：`docker build -t smart-parking-backend -f Dockerfile.java .`

### 前端（RuoYi-SpringBoot3-ElementPlus-master/）

- Vue3 + ElementPlus + Vite
- 编码规范见 `.cursorrules`
- 开发：`npm run dev`（proxy 到 localhost:8087）
- Docker 构建：`npm run build:prod`（nginx 多阶段构建）
- Docker 访问：http://localhost:80/admin/

### AI 推理（Parking_Ai/）

- FastAPI + YOLO (parking.pt)
- 编码规范见 `.cursorrules`
- 启动：`python -m uvicorn main_api:app --host 0.0.0.0 --port 8000`
- 模型文件禁止提交（.gitignore 已忽略）

## Docker

- healthcheck 统一使用 `wget`（alpine busybox 自带）
- 前端通过 nginx `/prod-api/` 代理到后端
- 前端 BASE_URL 在 Docker 中为 `/prod-api`（同源代理）

## Git 规范

commit message：`type: subject`，type：feat / fix / refactor / chore / docs

## 禁止事项

- 不要自动生成文档（README、设计文档等）
- 不要提交 .pt/.pth 模型文件
- 不要硬编码密码（使用环境变量）
- 不要使用 `mysql` MCP，必须用 `mysql-smartparking`
