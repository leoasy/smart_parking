# 开源来源说明

本项目为社区车位占用检测系统，包含开源框架二次开发内容和本项目自研业务内容。

## 1. 开源框架与第三方组件

后端使用 Spring Boot、Spring Security、MyBatis Plus、Druid、PageHelper、Quartz、Redis 客户端等 Java 生态组件。

前端使用 Vue3、Vite、Element Plus、Pinia、ECharts、Axios、UEditor Plus 等组件。

AI 服务使用 FastAPI、Uvicorn、OpenCV、YOLO 相关依赖和 Python 数据处理库。

离线提交包内的 `Redis/` 为 Windows 版 Redis 运行组件，用于本机运行依赖，不属于本项目原创实现；Redis 运行产生的数据文件不纳入归档。

项目中保留的 `LICENSE` 文件、依赖许可证和第三方组件声明应随代码归档一起提交。

## 2. 二次开发边界

基础后台框架能力包括用户权限、菜单、字典、日志、代码生成、通用布局等，不作为本项目原创能力声明。

本项目主要实现内容包括车位占用检测业务、AI 推理接口、ROI 车位区域处理、车位状态变化事件、告警记录、智慧大屏统计、数据库结构和本地部署脚本。

## 3. 归档包说明

Git 源码仓库默认不包含以下内容：

- `.env` 等本机环境变量文件。
- `node_modules`、`target`、`dist` 等构建产物。
- `.idea`、`.tmp`、日志、缓存和本地助手配置。
- `.pt`、`.pth`、`.onnx` 等模型权重文件。
- Redis 的 `dump.rdb`、PID、AOF 和日志等运行数据。
- AI 运行输出图片。

离线提交包为保证可运行性，包含 `Redis/` 运行组件和 `ai-service/model/parking.pt` 模型权重。模型为训练产物，不作为源码提交到 Git。
