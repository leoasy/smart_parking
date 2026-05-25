# 后端服务

社区车位占用检测系统后端服务，负责业务接口、状态更新、告警和 AI 检测结果落库。

## Local configuration

The service reads local overrides from the repository root `.env` file.

```powershell
cd D:\IDEA\smart-parking\backend
mvn.cmd -pl ruoyi-admin -am package -DskipTests
java -jar .\ruoyi-admin\target\RuoyiSpringBoot3.jar
```

## Required services

- MySQL: `127.0.0.1:3306`, database `smart_parking`
- Redis: `127.0.0.1:6379`
- AI inference: `http://127.0.0.1:8000`

Configure credentials through `.env` or process environment variables:

```text
MYSQL_USER=Server
MYSQL_PASSWORD=your_password
REDIS_PASSWORD=your_password
```
