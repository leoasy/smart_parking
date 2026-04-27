# =========================================
# RuoYi SpringBoot — 多阶段构建
# Java 17 + Maven → JRE 运行时
# =========================================

# ---- 构建阶段 ----
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /build

# ① 先拷贝 POM 文件，利用 Docker 缓存下载依赖
COPY pom.xml ./
COPY ruoyi-admin/pom.xml ./ruoyi-admin/pom.xml
COPY ruoyi-biz/pom.xml ./ruoyi-biz/pom.xml
COPY ruoyi-common/pom.xml ./ruoyi-common/pom.xml
COPY ruoyi-framework/pom.xml ./ruoyi-framework/pom.xml
COPY ruoyi-generator/pom.xml ./ruoyi-generator/pom.xml
COPY ruoyi-quartz/pom.xml ./ruoyi-quartz/pom.xml
COPY ruoyi-system/pom.xml ./ruoyi-system/pom.xml

RUN mvn dependency:go-offline -B

# ② 拷贝源码并打包
COPY . .
RUN mvn clean package -DskipTests -B

# ---- 运行阶段 ----
FROM eclipse-temurin:17-jre

WORKDIR /app

# 时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=build /build/ruoyi-admin/target/*.jar app.jar

# 上传文件挂载点（由 docker-compose 注入）
VOLUME ["/data/upload"]

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Dfile.encoding=UTF-8", \
    "-XX:+UseG1GC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-jar", "app.jar"]
