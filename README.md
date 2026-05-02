# Smart Parking 智慧停车系统

Smart Parking 是一个面向社区停车场的车位占用检测系统。项目由 RuoYi Spring Boot 后端、Vue3 管理前端、FastAPI + YOLO 推理服务、MySQL、Redis 和可选 AI 网关组成，支持车位状态管理、相机与 ROI 配置、AI 事件入库、告警去重与限流。

## 服务拓扑

| 服务 | 技术栈 | 默认地址 | 说明 |
| --- | --- | --- | --- |
| 前端 | Vue3 + Element Plus + Vite + Nginx | http://localhost/admin/ | 通过 `/prod-api/` 同源代理后端 |
| 后端 | RuoYi Spring Boot 3 + MySQL + Redis + Kafka | http://localhost:8087 | 容器内监听 8080，宿主机映射 8087 |
| AI 推理 | Python FastAPI + YOLO | http://localhost:8000 | 负责图片/视频检测并回调后端 |
| MySQL | MySQL 8.4 | localhost:3306 | 业务库 `smart_parking` |
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

```powershell
docker compose up -d
```

健康检查：

```powershell
curl http://localhost/health
curl http://localhost:8087/actuator/health
curl http://localhost:8000/health
```

访问入口：

```text
前端：http://localhost/admin/
账号：admin / 12345678
Druid：http://localhost:8087/druid/
```

停止服务：

```powershell
docker compose down
docker compose down -v  # 删除数据卷，谨慎使用
```

## 本地开发

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

```powershell
cd RuoYi-SpringBoot3-ElementPlus-master
npm.cmd install
npm.cmd run dev
npm.cmd run build:prod
npm.cmd run build:stage
npm.cmd audit
```

说明：

- 开发服务默认监听 80，`/prod-api` 代理到 `http://localhost:8087`。
- `build:prod` 只做 Vite 构建；FTP 部署已拆分到 `deploy:ftp`。
- `vite-plugin-svg-icons` 已被本地 Vite 插件替代，避免旧 `svg-baker/svgo/postcss` 漏洞链。
- `package-lock.json` 当前未跟踪，安全补丁通过 `package.json` 精确版本和 `overrides` 固定。

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
npm.cmd audit
npm.cmd run build:prod
npm.cmd run build:stage

cd ..
docker compose config
docker compose -f docker-compose.prod.yml config
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
