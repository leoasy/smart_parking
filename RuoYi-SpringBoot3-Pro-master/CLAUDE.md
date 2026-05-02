# 后端 Agent 说明

本目录是 Smart Parking 的 RuoYi Spring Boot 3 后端。

## 开发约束

- Java 版本按 17 处理。
- 修改业务逻辑时优先保持 RuoYi 既有分层：Controller -> Service -> Mapper。
- Kafka、Redis、数据库、AI 服务地址都必须从配置或环境变量读取。
- 不硬编码密码、Token、数据库连接串。
- 新增测试优先放在对应模块的 `src/test/java`。

## 重点模块

| 模块 | 说明 |
| --- | --- |
| `ruoyi-admin` | 应用入口和配置 |
| `ruoyi-biz` | 停车场业务、AI 事件、告警、Kafka 消费 |
| `ruoyi-common` | 公共工具、Redis、Trace、AI 封装 |
| `ruoyi-framework` | Spring Security 和 Web 基础配置 |

## 常用命令

```powershell
mvn.cmd test
mvn.cmd clean package -DskipTests
docker build -t smart-parking-backend -f Dockerfile.java .
curl http://localhost:8087/actuator/health
```

## 业务链路

```text
KafkaConsumerService
  -> ParkingDetectService
  -> Parking_Ai FastAPI
  -> AiEventServiceImpl
  -> AiAlarmOrchestrator
  -> AlarmServiceImpl
```

涉及数据库时必须遵守根目录 `AGENTS.md` 中的 `mysql-smartparking` MCP 约束。
