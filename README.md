# Smart Parking — 智慧停车场系统

基于计算机视觉的社区车位占用检测系统，支持实时车位监控、告警管理、区域管理。

## 技术架构

```
┌─────────────────────────────────────────────────────────┐
│  前端 Vue3 + ElementPlus (Nginx)    http://localhost:80 │
└─────────────────────────┬───────────────────────────────┘
                          │ /prod-api/
┌─────────────────────────▼───────────────────────────────┐
│  后端 RuoYi SpringBoot3 (Java 17)   http://localhost:8087 │
│  ┌──────────────┐   ┌──────────────────────────────┐   │
│  │ REST API     │   │ Kafka Consumer (parking_alarm)│   │
│  └──────────────┘   └──────────────┬───────────────┘   │
│                                     │                    │
│  ┌──────────────────────────────────▼───────────────┐   │
│  │  ParkingDetectService → AI 推理回调              │   │
│  │  AiAlarmOrchestrator → 告警去重 + 限流            │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│  AI 推理服务 (FastAPI + YOLO)   http://localhost:8000   │
│  Kafka Topic: parking_alarm → 消费 → 检测 → 推送后端    │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────┬───────────────────────────────┐
│  MySQL 8.4 (端口 3307)  │  Redis 7.4 (端口 6379)      │
│  smart_parking          │  会话 / 缓存 / 限流          │
└─────────────────────────┴───────────────────────────────┘
```

## 快速启动

### 前置条件

- Docker & Docker Compose
- 克隆项目后，先创建 `.env`（从 `.env` 复制并修改密码）

```bash
# 1. 复制环境变量文件
cp .env .env.bak

# 2. 启动所有服务（MySQL + Redis + 后端 + AI + 前端）
docker compose up -d

# 3. 验证所有服务健康
curl http://localhost:80/health    # 前端
curl http://localhost:8087/actuator/health  # 后端
curl http://localhost:8000/health  # AI

# 4. 访问系统
# 前端：http://localhost:80/admin/
# 账号：admin / 12345678
# Druid 监控：http://localhost:8087/druid/ (账号密码见 .env)
```

### 停止

```bash
docker compose down        # 保留数据卷
docker compose down -v     # 删除所有数据（慎用）
```

## 项目结构

```
smart-parking/
├── docker-compose.yml           # Docker 编排配置
├── docker-compose.prod.yml      # 生产环境配置（含 Prometheus/Grafana）
├── .env                         # 环境变量（敏感信息，不提交）
├── CLAUDE.md                    # 项目规范（AI Agent 使用）
│
├── RuoYi-SpringBoot3-Pro-master/   # Java 后端
│   ├── Dockerfile.java             # 多阶段构建
│   ├── ruoyi-admin/                # REST API + 管理后台入口
│   ├── ruoyi-biz/                  # 业务模块（停车/告警/Kafka）
│   ├── ruoyi-system/               # 系统模块（用户/角色/菜单）
│   └── ruoyi-framework/             # 框架配置（安全/Cache/跨域）
│
├── RuoYi-SpringBoot3-ElementPlus-master/  # Vue3 前端
│   ├── Dockerfile.nginx             # 前端 Nginx 构建
│   ├── vite.config.js               # Vite 配置（含 /prod-api 代理）
│   └── docker/nginx.conf            # Nginx 配置
│
├── Parking_Ai/                    # Python AI 推理服务
│   ├── main_api.py                # FastAPI 入口（端口 8000）
│   ├── core/
│   │   ├── engine.py               # 车位检测调度引擎
│   │   ├── roi_loader.py           # ROI 区域加载
│   │   └── config.py              # 配置读取
│   ├── inference/
│   │   ├── detector.py             # YOLO 车辆检测
│   │   ├── roi_matcher.py          # ROI 匹配
│   │   └── stability.py            # 稳定性判断
│   ├── model/parking.pt            # YOLO 模型文件（不提交）
│   └── requirements.txt            # Python 依赖
│
├── smart_parking.sql             # 数据库初始化脚本
├── prometheus/
│   ├── prometheus.yml             # Prometheus 抓取配置
│   └── rules/alerts.yml           # Prometheus 告警规则
└── grafana/
    └── provisioning/              # Grafana 自动配置
```

## 核心业务表

| 表名 | 说明 |
|------|------|
| `biz_region` | 区域管理 |
| `dev_camera` | 相机设备 |
| `biz_parking_roi` | ROI 区域配置 |
| `biz_parking_slot` | 车位状态 |
| `ai_event` | AI 推理事件 |
| `biz_alarm` | 告警记录 |

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3307 | 数据库 |
| Redis | 6379 | 缓存/限流 |
| 后端 | 8087 | Java Spring Boot |
| AI | 8000 | FastAPI YOLO 推理 |
| 前端 | 80 | Nginx |
| AI 网关 | 3000 | new-api（可选，需 `docker compose --profile gateway up`） |
| Prometheus | 9090 | 指标收集（生产） |
| Grafana | 3000 | 可视化面板（生产） |

## 开发说明

### 后端

```bash
cd RuoYi-SpringBoot3-Pro-master
mvn clean package -DskipTests
docker build -t smart-parking-backend -f Dockerfile.java .
```

### 前端

```bash
cd RuoYi-SpringBoot3-ElementPlus-master
npm install
npm run dev      # 开发模式（proxy 到 localhost:8087）
npm run build:prod  # 生产构建
```

### AI 推理

```bash
cd Parking_Ai
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
python -m uvicorn main_api:app --host 0.0.0.0 --port 8000
```

## 监控（生产环境）

启动监控栈：

```bash
docker compose -f docker-compose.prod.yml up -d
```

- Prometheus：http://localhost:9090
- Grafana：http://localhost:3000 （admin / 密码见 .env）

## Git 规范

commit message 格式：`type: subject`

type：feat / fix / refactor / chore / docs / perf / test

## 注意事项

- YOLO 模型文件（`Parking_Ai/model/parking.pt`）不提交，已在 `.gitignore` 中忽略
- 所有密码通过 `.env` 环境变量注入，禁止硬编码
- 登录账号：`admin` / `12345678`
