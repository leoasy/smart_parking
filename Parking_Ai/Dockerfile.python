# =========================================
# Parking AI — Python FastAPI + Ultralytics
# =========================================

FROM python:3.11-slim

WORKDIR /app

# 系统依赖（OpenCV 需要 libGL，Ultralytics 需要基础工具）
RUN apt-get update && apt-get install -y --no-install-recommends \
        libgl1-mesa-glx \
        libglib2.0-0 \
        libsm6 \
        libxext6 \
        libxrender-dev \
        libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# 时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Python 依赖（使用清华镜像加速）
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt \
    -i https://pypi.tuna.tsinghua.edu.cn/simple

# 拷贝应用代码（模型通过 volume 挂载，避免镜像过大）
COPY . .

# 模型文件较大，不作为镜像一部分；docker-compose 中从宿主机挂载
VOLUME ["/app/model"]

EXPOSE 8000

# 生产模式（关闭 reload）
CMD ["uvicorn", "main_api:app", "--host", "0.0.0.0", "--port", "8000"]
