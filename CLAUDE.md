# Smart Parking 项目规范

## 数据库 MCP

**必须使用 `mysql-smartparking` MCP**，禁止使用 `mysql` MCP。

| MCP | 数据库 | 用途 |
|-----|--------|------|
| `mysql-smartparking` | `smart_parking` | 本项目所有业务数据 |
| `mysql` | `finpilot_dev` | FinPilot 项目（无关） |

### 常用工具映射

所有数据库操作均通过 `mcp_mysql_smartparking_*` 调用：
- 查询：`mcp_mysql_smartparking_run_select_query`
- 写入：`mcp_mysql_smartparking_execute_write_query`
- DDL：`mcp_mysql_smartparking_execute_ddl`
- 事务：`mcp_mysql_smartparking_begin_transaction` / `commit` / `rollback`

### 数据库连接

```
Host: 127.0.0.1:3306
User: Server
Password: 123456
Database: smart_parking
```

### 系统登录

RuoYi 管理后台管理员账号：
- 用户名：admin
- 密码：12345678

## 项目架构

### 子模块

| 模块 | 技术栈 | 说明 |
|------|--------|------|
| `RuoYi-SpringBoot3-Pro-master/` | Java Spring Boot 3 | REST API、管理后台、Kafka Consumer |
| `Parking_Ai/` | Python FastAPI + YOLO | 视觉 AI 车位检测服务 |

### 核心业务表

```
ai_event          — AI 事件记录
biz_alarm         — 告警记录
biz_parking_slot  — 车位状态
dev_camera        — 相机设备
biz_parking_roi  — ROI 区域
biz_region        — 区域管理
```

### 系统表（RuoYi 通用）

sys_user, sys_role, sys_menu, sys_dept, sys_dict_type, sys_dict_data,
sys_job, sys_job_log, sys_logininfor, sys_oper_log, sys_notice,
qrtz_* (定时任务调度), gen_table, gen_table_column, magic_* 等

## 子模块规范

详细规范见各子模块的 CLAUDE.md：
- `RuoYi-SpringBoot3-Pro-master/CLAUDE.md`
- `Parking_Ai/CLAUDE.md`

## 禁止事项

- **不要自动生成文档**：除非用户明确要求，不创建 README、设计文档、架构说明
- **不要提交大文件**：模型文件（*.pt）禁止提交，已在 .gitignore 中忽略
- **不要硬编码配置**：敏感配置通过环境变量或 .env 管理
- **不要使用 mysql MCP**：必须使用 mysql-smartparking MCP

## Git 规范

commit message 格式：`type: subject`，type 取值：feat / fix / refactor / chore / docs

## 环境变量

参考各子模块的 `.env.example`，敏感信息不提交到 Git。
