> Always respond in 中文

## 项目概述
RuoYi-SpringBoot3-Pro-master 是 smart-parking 的 Spring Boot 3 后端，提供 REST API、管理后台、 Kafka 消息消费。

## 架构要点
```
Kafka Topic (parking_alarm)
    ↓
RuoYi Backend (Kafka Consumer)
    → ParkingDetectService (调用 AI 服务 HTTP API)
    → AiAlarmOrchestrator (告警去重 + 限流)
    ↓
告警记录入库
```

## 关键模块
- `ruoyi-admin`：管理后台、REST API 入口
- `ruoyi-biz`：业务模块（停车相关业务逻辑）
- `ruoyi-system`：系统管理（用户、角色、菜单）
- `ruoyi-common`：公共组件（工具类、响应封装）

## 编码规范
- 使用 Lombok（@Data, @Slf4j 等），不写重复 getter/setter
- Service 层禁止硬编码 Kafka Bootstrap Servers，从 `@ConfigurationProperties` 注入
- 新增 Controller 要在模块的 Router 注册
- REST 统一返回格式使用 `R` 类

## 构建与运行
```powershell
# 本地启动（确保 MySQL 和 Kafka 已就绪）
cd RuoYi-SpringBoot3-Pro-master
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Docker 构建
docker build -t smart-parking-backend .

# 健康检查
curl http://localhost:8087/actuator/health
```

## 配置文件
- Docker 配置：`src/main/resources/application-docker.yml`
- 参考 `application-secure-template.yml` 管理敏感字段
- 数据库连接：smart_parking（见根目录 CLAUDE.md）

## document规范
- 默认不创建新的说明文档或文档文件
- 不要自动生成 README、设计文档、架构说明，除非用户明确要求
- 避免输出与代码无关的说明性文档
