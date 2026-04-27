> Always respond in 中文

## 项目概述
Parking_Ai 是 smart-parking 的 FastAPI AI 推理服务，基于 YOLO 模型做车位检测。

## 技术栈
- FastAPI + YOLO (parking.pt)
- Python 3.x + uvicorn
- HTTP API 供 RuoYi 后端调用

## 目录结构
- `main.py`：主应用入口
- `main_api.py`：推理 API 入口（独立端口）
- `app/api.py`：API 路由定义
- `app/schemas.py`：Pydantic 数据模型
- `app/config.py`：配置管理
- `app/deps.py`：依赖注入
- `model/`：YOLO 模型文件目录
- `inference/`：推理核心逻辑

## 编码规范
- 使用 type hints 标注所有函数参数和返回值
- 依赖声明在 `requirements.txt`，优先使用国内镜像安装
- API 路由放在 `app/api.py`，数据模型放在 `app/schemas.py`

## 启动与测试
```powershell
# 进入 Python 服务目录
cd Parking_Ai

# 安装依赖（使用国内镜像）
python -m pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 推理服务启动（端口 8000）
python -m uvicorn main_api:app --reload --host 0.0.0.0 --port 8000

# 健康检查
curl http://localhost:8000/health
```

## 环境变量
- 参考 `.env.example`，不将 `.env` 提交到 GitHub
- 模型路径、阈值、相机配置等通过环境变量注入

## 模型管理
- 模型文件 `parking.pt` 禁止提交到 Git（已在 .gitignore）
- 首次运行自动下载模型到 `model/` 目录

## document规范
- 默认不创建新的说明文档或文档文件
- 不要自动生成 README、设计文档，除非用户明确要求
