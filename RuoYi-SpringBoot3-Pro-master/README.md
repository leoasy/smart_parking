# Smart Parking 后端

本目录是 Smart Parking 的 Java 后端，基于 RuoYi Spring Boot 3 改造，负责系统管理、停车业务接口、Kafka 消费、AI 检测回调、告警编排和数据库持久化。

## 技术栈

| 组件 | 当前配置 |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.5.8 |
| ORM | MyBatis / MyBatis-Plus 相关模块并存 |
| 数据库 | MySQL 8.x |
| 缓存 | Redis |
| 消息 | Kafka |
| JSON | fastjson2 |
| 代码生成/系统管理 | RuoYi 模块 |

## 模块说明

```text
ruoyi-admin      应用入口、REST API、配置文件
ruoyi-framework  Spring Security、过滤器、Web 配置
ruoyi-system     用户、角色、菜单、字典、参数等系统功能
ruoyi-common     公共工具、Redis、响应封装、AI SDK 封装
ruoyi-biz        停车业务、AI 事件、告警、Kafka、OSS 工具
ruoyi-quartz     定时任务
ruoyi-generator  代码生成
```

## 关键链路

```text
Kafka Topic: parking_alarm
  -> KafkaConsumerService
  -> ParkingDetectService
  -> AI FastAPI 服务
  -> AiEventServiceImpl 写入 ai_event
  -> AiAlarmOrchestrator 去重/限流
  -> AlarmServiceImpl 写入 biz_alarm
```

## 本地构建

```powershell
mvn.cmd test
mvn.cmd clean package -DskipTests
```

Docker 构建：

```powershell
docker build -t smart-parking-backend -f Dockerfile.java .
```

Docker Compose 会传入：

```text
SPRING_PROFILES_ACTIVE=devmy,docker
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/smart_parking...
SPRING_DATA_REDIS_HOST=redis
AI_FASTAPI_URL=http://parking-ai:8000
```

宿主机访问端口为 8087。

## 配置文件

| 文件 | 说明 |
| --- | --- |
| `ruoyi-admin/src/main/resources/application.yml` | 主配置 |
| `ruoyi-admin/src/main/resources/application-docker.yml` | Docker 环境覆盖 |
| `Dockerfile.java` | 多阶段 Maven 构建和运行镜像 |

敏感配置应通过环境变量传入，不要写死在 yml 或 Java 代码中。

## 验证

```powershell
mvn.cmd test
mvn.cmd clean package -DskipTests
curl http://localhost:8087/actuator/health
```

已修复的工程问题包括 Java 25 下 JaCoCo 兼容、Lombok 版本、Kafka fastjson2 使用、Redis 自增返回值、Spring Security 注入、TraceFilter 兼容和相关单测。
