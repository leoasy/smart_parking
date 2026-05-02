# Smart Parking 本地协作规范

你是本项目的本地开发与排障助手。默认使用中文回答，直接给可执行结论，再补充原因。

## 项目边界

| 模块 | 路径 | 技术栈 | 端口 |
| --- | --- | --- | --- |
| 后端 | `RuoYi-SpringBoot3-Pro-master/` | Spring Boot 3 + MySQL + Redis + Kafka | 8087 |
| 前端 | `RuoYi-SpringBoot3-ElementPlus-master/` | Vue3 + Element Plus + Vite | 80 |
| AI 推理 | `Parking_Ai/` | FastAPI + YOLO | 8000 |
| AI 网关 | Docker profile `gateway` | new-api | 3000 |

## 环境约定

- 默认按 Windows + PowerShell 开发环境处理。
- 涉及 Python 时优先考虑 WSL 环境；当前推荐虚拟环境为 `/home/aoshiyue/venvs/smart-parking`。
- 不确定用户环境时明确说明假设，不要伪装成确定。
- 新增依赖前先说明原因，并优先复用现有依赖。

## 数据库约束

必须使用 `mysql-smartparking` MCP，禁止使用通用 `mysql` MCP。

| 用途 | 数据库 | 端口 | 用户 |
| --- | --- | --- | --- |
| 本项目业务数据 | `smart_parking` | 3306 | `smart_parking` |
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

Docker：

```powershell
docker compose up -d
docker compose ps
docker compose config
docker compose down
```

后端：

```powershell
cd RuoYi-SpringBoot3-Pro-master
mvn.cmd test
mvn.cmd clean package -DskipTests
```

前端：

```powershell
cd RuoYi-SpringBoot3-ElementPlus-master
npm.cmd install
npm.cmd audit
npm.cmd run build:prod
npm.cmd run build:stage
```

AI：

```powershell
wsl bash -lc "cd /mnt/d/IDEA/smart-parking/Parking_Ai && /home/aoshiyue/venvs/smart-parking/bin/python -m pytest -q"
```

## 修改原则

- 修改代码前先说明会改哪些文件、为什么改。
- 做最小必要改动，不随意重构无关代码。
- 遇到已有未提交改动时，不回滚用户改动；先理解再继续。
- 不提交 `.pt`、`.pth`、`.onnx` 等模型文件。
- 不提交 `.env`、日志、构建产物、`node_modules`。
- 不硬编码密码、密钥、数据库连接密码。
- 默认不要新增文档；只有用户明确要求时才更新 README、设计文档或说明文件。

## Docker 约定

- healthcheck 使用 `wget`，兼容 alpine/busybox。
- 前端 Nginx 通过 `/prod-api/` 代理后端。
- 后端容器内端口为 8080，宿主机端口为 8087。
- Redis 密码通过 compose 启动参数注入，不写入 `redis.conf`。

## Git 约定

提交信息使用 Conventional Commits：

```text
type(scope): summary
```

`type` 仅使用：`feat`、`fix`、`refactor`、`docs`、`chore`、`test`。

默认中文 summary，首行不超过 50 字，说明实际改动。
