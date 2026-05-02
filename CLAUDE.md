# Smart Parking Claude / Agent 说明

本文件与 `AGENTS.md` 保持同一套项目约束，供 Claude、Hermes 或其他本地 Agent 读取。

## 工作方式

- 默认中文回答。
- 默认 Windows PowerShell 命令；Python 相关优先使用 WSL。
- 修改前说明文件范围和原因。
- 不回滚用户已有改动。
- 不硬编码密码，不提交模型文件、`.env`、日志和构建产物。
- 用户未明确要求时，不主动新增 README、设计文档或说明文档。

## 项目模块

| 模块 | 路径 | 启动/验证 |
| --- | --- | --- |
| 后端 | `RuoYi-SpringBoot3-Pro-master/` | `mvn.cmd test`、`mvn.cmd clean package -DskipTests` |
| 前端 | `RuoYi-SpringBoot3-ElementPlus-master/` | `npm.cmd audit`、`npm.cmd run build:prod` |
| AI | `Parking_Ai/` | WSL 中运行 pytest 和 uvicorn |
| Docker | 根目录 | `docker compose up -d`、`docker compose config` |

## 数据库要求

数据库操作必须走 `mysql-smartparking` MCP，禁止使用通用 `mysql` MCP。

业务库：`smart_parking`。

核心表：

```text
ai_event
biz_alarm
biz_parking_slot
dev_camera
biz_parking_roi
biz_region
```

## 常用命令

```powershell
docker compose up -d
curl http://localhost/health
curl http://localhost:8087/actuator/health
curl http://localhost:8000/health
```

```powershell
cd RuoYi-SpringBoot3-Pro-master
mvn.cmd test
mvn.cmd clean package -DskipTests
```

```powershell
cd RuoYi-SpringBoot3-ElementPlus-master
npm.cmd install
npm.cmd audit
npm.cmd run build:prod
npm.cmd run build:stage
```

```powershell
wsl bash -lc "cd /mnt/d/IDEA/smart-parking/Parking_Ai && /home/aoshiyue/venvs/smart-parking/bin/python -m pytest -q"
```
