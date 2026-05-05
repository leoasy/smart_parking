# Smart Parking 智慧停车系统

Smart Parking 是一个面向社区停车场的车位占用检测系统。项目由 RuoYi Spring Boot 后端、Vue3 管理前端、FastAPI + YOLO 推理服务、MySQL、Redis 和可选 AI 网关组成，支持车位状态管理、相机与 ROI 配置、AI 事件入库、告警去重与限流。

## 服务拓扑

| 服务 | 技术栈 | 默认地址 | 说明 |
| --- | --- | --- | --- |
| 前端 | Vue3 + Element Plus + Vite + Nginx | http://localhost:8088/admin/ | 通过 `/prod-api/` 同源代理后端，端口由 `UI_HOST_PORT` 控制 |
| 后端 | RuoYi Spring Boot 3 + MySQL + Redis + Kafka | http://localhost:8087 | 容器内监听 8080，宿主机映射 8087 |
| AI 推理 | Python FastAPI + YOLO | http://localhost:8000 | 负责图片/视频检测并回调后端 |
| MySQL | MySQL 8.4 | localhost:3307 | 业务库 `smart_parking`，容器内仍为 `mysql:3306` |
| Redis | Redis 7.4 | localhost:6379 | 会话、缓存、限流 |
| AI 网关 | new-api | http://localhost:3000 | 可选 profile：`gateway` |

## 目录结构

```text
smart-parking/
├─ docker-compose.yml                         # 本地开发编排
├─ docker-compose.prod.yml                    # 生产/监控编排
├─ nginx.conf                                 # 前端反向代理配置
├─ redis.conf                                 # Redis 基础配置，密码由 compose 注入
├─ smart_parking.sql                          # 数据库初始化脚本
├─ database_optimization_indexes.sql          # 索引优化脚本
├─ RuoYi-SpringBoot3-Pro-master/              # Java 后端
├─ RuoYi-SpringBoot3-ElementPlus-master/      # Vue3 前端
└─ Parking_Ai/                                # Python AI 推理服务
```

## 快速启动

前提：Windows 本机安装 Docker Desktop，并确保 `.env` 已存在且包含 MySQL、Redis、Druid 等密码变量。模型文件 `Parking_Ai/model/parking.pt` 不提交到 Git，需要本地放置。

首次配置：

```powershell
cd D:\IDEA\smart-parking
Copy-Item .env.example .env
# 修改 .env 中的 MYSQL_ROOT_PASSWORD、MYSQL_PASSWORD、REDIS_PASSWORD、DRUID_LOGIN_PASSWORD 等密码
```

正常情况下可以一条命令启动全部服务：

```powershell
cd D:\IDEA\smart-parking

docker compose up -d
docker compose ps
```

首次启动、排障或需要观察依赖初始化时，推荐分阶段启动，便于定位数据库、缓存或后端健康检查问题：

```powershell
cd D:\IDEA\smart-parking

docker compose config
docker compose up -d mysql redis
docker compose up -d ruoyi-server parking-ai ruoyi-ui
docker compose ps
```

健康检查：

```powershell
curl http://localhost:8088/health
curl http://localhost:8087/actuator/health
curl http://localhost:8000/health
```

说明：

- `docker compose ps` 应显示 `mysql`、`redis`、`ruoyi-server`、`parking-ai`、`ruoyi-ui` 都为 `healthy`。
- `http://localhost:8087/actuator/health` 可能返回认证失败 JSON，说明 HTTP 已到达后端；Docker 内部 healthcheck 使用容器内地址判断服务健康。
- Docker MySQL 默认映射到宿主机 `localhost:3307`，避免和 Windows 本机 MySQL 的 `3306` 冲突。容器内后端仍访问 `mysql:3306`，不受宿主机端口影响。
- Docker 前端建议映射到宿主机 `localhost:8088`，避免 Windows 本机或其他容器占用 `80`。如果你把 `.env` 的 `UI_HOST_PORT` 改回 `80`，访问地址才是 `http://localhost/admin/`。

访问入口：

```text
前端：http://localhost:8088/admin/
账号：admin / 12345678
Druid：http://localhost:8087/druid/
```

可选 AI 网关：

```powershell
docker compose --profile gateway up -d
```

访问地址：

```text
AI 网关：http://localhost:3000
```

停止服务：

```powershell
docker compose down
docker compose down -v  # 删除数据卷，谨慎使用
```

`docker compose down -v` 会删除 MySQL、Redis、上传目录等数据卷。只有需要重新导入 `smart_parking.sql` 或清空本地数据时才使用。

## 本地开发

推荐开发方式：基础服务、后端和 AI 仍由 Docker 管理，前端用 Vite 本机热更新。

```powershell
cd D:\IDEA\smart-parking
docker compose up -d mysql redis ruoyi-server parking-ai
```

### 后端

```powershell
cd RuoYi-SpringBoot3-Pro-master
mvn.cmd test
mvn.cmd clean package -DskipTests
```

Docker 构建：

```powershell
docker build -t smart-parking-backend -f Dockerfile.java .
```

### 前端

本地开发推荐使用 production mode，因为当前 `vite.config.js` 代理的是 `/prod-api`，而 `.env.development` 使用 `/dev-api`。直接运行 `npm.cmd run dev` 可能导致接口代理不通。

```powershell
cd D:\IDEA\smart-parking\RuoYi-SpringBoot3-ElementPlus-master
npm.cmd ci
npm.cmd run dev -- --mode production --port 5173
```

访问：

```text
http://localhost:5173/admin/
```

构建和检查：

```powershell
npm.cmd run build:prod
npm.cmd run build:stage
npm.cmd audit
```

说明：

- Vite 默认配置监听 80；本地开发建议显式指定 `--port 5173`，避免占用系统 80 端口。
- `/prod-api` 代理到 `http://localhost:8087`。
- `build:prod` 只做 Vite 构建；FTP 部署已拆分到 `deploy:ftp`。
- `vite-plugin-svg-icons` 已被本地 Vite 插件替代，避免旧 `svg-baker/svgo/postcss` 漏洞链。
- `package-lock.json` 已跟踪，CI 和本地开发统一使用 `npm ci`。

### AI 推理

该项目原先通过 WSL 中的 hermes agent 修改，Python 环境也在 WSL。推荐继续在 WSL 中使用已有虚拟环境：

```powershell
wsl bash -lc "cd /mnt/d/IDEA/smart-parking/Parking_Ai && /home/aoshiyue/venvs/smart-parking/bin/python -m pytest -q"
wsl bash -lc "cd /mnt/d/IDEA/smart-parking/Parking_Ai && /home/aoshiyue/venvs/smart-parking/bin/python -m uvicorn main_api:app --host 0.0.0.0 --port 8000"
```

全新环境可按下面安装：

```bash
cd /mnt/d/IDEA/smart-parking/Parking_Ai
python -m venv /home/aoshiyue/venvs/smart-parking
/home/aoshiyue/venvs/smart-parking/bin/python -m pip install -r requirements.txt
```

## 核心业务表

| 表名 | 说明 |
| --- | --- |
| `biz_region` | 区域管理 |
| `dev_camera` | 相机设备 |
| `biz_parking_roi` | ROI 区域 |
| `biz_parking_slot` | 车位状态 |
| `ai_event` | AI 事件记录 |
| `biz_alarm` | 告警记录 |

## 关键链路

```text
相机/图片输入
  -> Parking_Ai YOLO 检测
  -> ROI 匹配与车位状态判断
  -> 后端 /biz/parking/detect 回调
  -> ai_event 记录
  -> AiAlarmOrchestrator 去重、限流、生成 biz_alarm
  -> 前端展示车位状态和告警
```

Kafka Topic `parking_alarm` 由后端消费，消费后会触发 `ParkingDetectService` 调用 AI 服务。

## 配置要点

- 不要提交 `.env`、模型文件、日志、构建产物和 `node_modules`。
- 密码通过环境变量注入，禁止在代码中硬编码。
- Redis 密码由 `docker-compose.yml` 通过 `--requirepass "$${REDIS_PASSWORD}"` 注入。
- 后端容器内 `SERVER_PORT=8080`，宿主机通过 `${RUOYI_HOST_PORT:-8087}:8080` 映射。
- 前端 Docker 构建使用 `/prod-api`，由 Nginx 代理到后端。
- Docker healthcheck 使用 `wget`，兼容 alpine/busybox 镜像。

## 验证清单

当前已验证通过的命令：

```powershell
cd RuoYi-SpringBoot3-Pro-master
mvn.cmd test
mvn.cmd clean package -DskipTests

cd ..\RuoYi-SpringBoot3-ElementPlus-master
npm.cmd ci
npm.cmd audit
npm.cmd run build:prod
npm.cmd run build:stage

cd ..
docker compose config
docker compose -f docker-compose.prod.yml config
docker compose build ruoyi-ui
```

AI 测试：

```powershell
wsl bash -lc "cd /mnt/d/IDEA/smart-parking/Parking_Ai && /home/aoshiyue/venvs/smart-parking/bin/python -m pytest -q"
```

注意：`docker compose -f docker-compose.prod.yml config` 若提示 `C:\Users\aoshiyue\.docker\config.json` 权限问题，属于本机 Docker 配置权限，不是项目配置错误。

## Git 规范

提交信息使用 Conventional Commits 风格：

```text
fix(scope): 修复具体问题
feat(scope): 新增具体功能
docs(scope): 更新项目文档
chore(scope): 调整构建或工程配置
test(scope): 补充或修复测试
refactor(scope): 重构但不改变行为
```

默认远程仓库：`origin https://github.com/leoasy/smart_parking.git`。
