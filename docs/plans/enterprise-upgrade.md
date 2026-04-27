# Smart Parking 企业级升级实施计划

> **执行方式**: 使用 subagent-driven-development skill，Claude Code 执行每个任务
> **代码修改必须通过 Claude Code**，禁止直接 patch/write_file

**目标**: 将 smart-parking 从"能跑的项目"升级为"企业级生产项目"

**五大升级领域**:
1. 基础设施 (Docker + CI/CD + 覆盖率)
2. 可观测性 (Actuator + Prometheus + 日志 + 链路追踪)
3. 弹性 (Redis + 熔断 + 限流)
4. API 品质 (OpenAPI + 异常处理 + 拦截器 + 版本管理)
5. 代码质量 (测试覆盖率 + 检查工具 + 提交规范)

---

## 阶段一: 基础设施 (1/5)

### 任务 1.1: Java 后端添加 Maven 插件 (JaCoCo + SpotBugs + Checkstyle)
**Objective**: 为 ruoyi 模块添加代码质量检查和覆盖率报告支持
**Files**:
- Modify: `RuoYi-SpringBoot3-Pro-master/pom.xml` — 添加 pluginManagement

**Step 1: 添加 pluginManagement**

```xml
<!-- properties 中添加 -->
<spotbugs.version>4.8.6.4</spotbugs.version>
<checkstyle.version>10.18.1</checkstyle.version>
<jacoco.version>0.8.12</jacoco.version>

<!-- plugins -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
</plugin>
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>${spotbugs.version}</version>
</plugin>
<plugin>
    <groupId>org.apache.rat</groupId>
    <artifactId>apache-rat-plugin</artifactId>
    <version>0.16.1</version>
</plugin>
```

**Step 2: 在 ruoyi-biz/pom.xml 的 build/plugins 中添加**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.60</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <configuration>
        <effort>Max</effort>
        <threshold>Medium</threshold>
        <xmlOutput>true</xmlOutput>
    </configuration>
</plugin>
```

**Step 3: 添加 .spotbugs*.xml 配置文件**
创建: `RuoYi-SpringBoot3-Pro-master/spotbugs-exclude.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<FindBugsFilter>
    <Match>
        <Bug pattern="EI_EXPOSE_REP,EI_EXPOSE_REP2"/>
    </Match>
    <Match>
        <Class name="~.*\.Test"/>
    </Match>
</FindBugsFilter>
```

**Step 4: 添加 checkstyle 配置**
创建: `RuoYi-SpringBoot3-Pro-master/checkstyle.xml`

**Step 5: 验证**
```bash
cd RuoYi-SpringBoot3-Pro-master
mvn clean test jacoco:report
# 预期: target/site/jacoco/index.html 生成覆盖率报告
```

---

### 任务 1.2: Python AI 服务添加测试覆盖率和代码检查
**Objective**: Parking_Ai 添加 pytest-cov + ruff + mypy
**Files**:
- Modify: `Parking_Ai/requirements.txt` — 添加 pytest-cov, ruff, mypy
- Create: `Parking_Ai/pyproject.toml` — 项目配置

**Step 1: 修改 requirements.txt**
添加行:
```
pytest-cov>=4.1.0
ruff>=0.1.0
mypy>=1.7.0
httpx>=0.25.0
```

**Step 2: 创建 pyproject.toml**
```toml
[project]
name = "parking-ai"
version = "1.0.0"

[tool.pytest.ini_options]
testpaths = ["tests"]
python_files = ["test_*.py"]
addopts = "--cov=app --cov-report=term-missing --cov-fail-under=60"

[tool.coverage.run]
source = ["app"]
omit = ["*/tests/*", "*/__pycache__/*"]

[tool.ruff]
line-length = 120
select = ["E", "F", "W", "I", "N", "UP", "SIM"]
ignore = ["E501"]

[tool.mypy]
python_version = "3.10"
warn_return_any = true
warn_unused_configs = true
disallow_untyped_defs = true
```

**Step 3: 添加 ruff 检查到 GitHub Actions**

---

### 任务 1.3: Docker 容器化
**Objective**: 为 Java 和 Python 服务创建 Dockerfile 和 docker-compose
**Files**:
- Create: `Dockerfile.java` (在 RuoYi-SpringBoot3-Pro-master/)
- Create: `Dockerfile.python` (在 Parking_Ai/)
- Create: `docker-compose.yml` (在项目根目录)

**Step 1: 创建 Dockerfile.java**
基于 multi-stage build:
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY ruoyi-*/pom.xml ruoyi-*/pom.xml
COPY ruoyi-biz/pom.xml ruoyi-biz/pom.xml
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/ruoyi-admin/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", "-Dspring.profiles.active=prod", "app.jar"]
```

**Step 2: 创建 Dockerfile.python**
```dockerfile
FROM python:3.10-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
COPY . .
EXPOSE 8000
CMD ["uvicorn", "main_api:app", "--host", "0.0.0.0", "--port", "8000"]
```

**Step 3: 创建 docker-compose.yml**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: smart_parking
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  ruoyi-backend:
    build:
      context: ./RuoYi-SpringBoot3-Pro-master
      dockerfile: Dockerfile.java
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/smart_parking?useSSL=false
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123456

  parking-ai:
    build:
      context: ./Parking_Ai
      dockerfile: Dockerfile.python
    ports:
      - "8000:8000"
    deploy:
      resources:
        limits:
          memory: 4G
        reservations:
          devices:
            - driver: nvidia
              count: all
              capabilities: [gpu]

volumes:
  mysql_data:
```

**Step 4: 验证**
```bash
docker-compose build
docker-compose up -d
```

---

### 任务 1.4: GitHub Actions CI/CD
**Objective**: 创建完整的 CI/CD 流水线
**Files**:
- Create: `.github/workflows/ci.yml`
- Create: `.github/workflows/docker.yml`

**ci.yml 内容**:
```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  java-ci:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'
      - name: Run tests with coverage
        run: mvn clean test jacoco:report
      - name: SpotBugs
        run: mvn spotbugs:check
      - name: Upload coverage
        uses: codecov/codecov-action@v4

  python-ci:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.10'
      - run: pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
      - run: pytest --cov=app --cov-report=xml
      - run: ruff check app/
      - run: mypy app/ --ignore-missing-imports
      - name: Upload coverage
        uses: codecov/codecov-action@v4

  docker-build:
    needs: [java-ci, python-ci]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - uses: docker/setup-buildx-action@v3
      - uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      - uses: docker/metadata@v5
        id: meta
        with:
          images: ${{ secrets.DOCKER_USERNAME }}/smart-parking
      - uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
```

---

## 阶段二: 可观测性 (2/5)

### 任务 2.1: Spring Boot Actuator + Micrometer Prometheus
**Objective**: 添加健康检查、指标暴露、Prometheus 抓取端点
**Files**:
- Modify: `RuoYi-SpringBoot3-Pro-master/ruoyi-biz/pom.xml` — 添加 actuator + micrometer
- Modify: `RuoYi-SpringBoot3-Pro-master/ruoyi-framework/src/main/resources/application.yml` — 配置端点

**Step 1: 在 ruoyi-biz/pom.xml dependencies 中添加**
```xml
<!-- Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<!-- Micrometer Prometheus -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Step 2: application.yml 添加**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
```

**Step 3: 验证**
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/prometheus
```

---

### 任务 2.2: 结构化日志 (Logback JSON + MDC)
**Objective**: 日志改为 JSON 格式，添加 traceId, spanId, userId
**Files**:
- Create: `RuoYi-SpringBoot3-Pro-master/ruoyi-framework/src/main/resources/logback-spring.xml`
- Modify: `RuoYi-SpringBoot3-Pro-master/ruoyi-framework/src/main/resources/application.yml` — 添加 trace 配置

**Step 1: 创建 logback-spring.xml**
添加 JSON encoder + MDC filter:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <springProperty scope="context" name="APP_NAME" source="spring.application.name"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>{"time":"%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ}","level":"%level","logger":"%logger{36}","message":"%msg","thread":"%thread","app":"${APP_NAME}","traceId":"%X{traceId:-}","spanId":"%X{spanId:-}","userId":"%X{userId:-}}%n</pattern>
        </encoder>
    </appender>
    
    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>
</configuration>
```

**Step 2: 添加 OpenTelemetry trace filter**
创建: `ruoyi-framework/src/main/java/com/ruoyi/framework/config/TraceFilter.java`

**Step 3: 验证**
```bash
# 本地启动后查看日志格式
curl http://localhost:8080/actuator/health
tail -f logs/ruoyi.log | jq  # 如果有 jq
```

---

### 任务 2.3: OpenTelemetry 链路追踪
**Objective**: 为 Java 后端添加分布式追踪
**Files**:
- Modify: `ruoyi-biz/pom.xml` — 添加 otel dependencies
- Create: `ruoyi-framework/src/main/java/com/ruoyi/framework/config/TracingConfig.java`

**Step 1: pom.xml 添加**
```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
    <version>1.36.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-instrumentation-annotations</artifactId>
    <version>2.1.0</version>
</dependency>
```

---

## 阶段三: 弹性 (3/5)

### 任务 3.1: Redis 缓存
**Objective**: 添加 Redis 作为缓存层（目前已有 spring-boot-starter-data-redis）
**Files**:
- Modify: `application.yml` — 配置 Redis 连接
- Create: `CacheConfig.java` — 缓存配置类

**Step 1: application.yml 添加**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 5s
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

**Step 2: 创建 CacheConfig.java**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(factory).cacheDefaults(config).build();
    }
}
```

**Step 3: 在核心 Service 方法上添加 @Cacheable**
例如: `AlarmServiceImpl.selectPageAlarmListWithDetails()` 等查询方法

---

### 任务 3.2: Resilience4j 熔断 + 重试
**Objective**: HTTP 调用和数据库操作添加熔断保护
**Files**:
- Modify: `ruoyi-biz/pom.xml` — 添加 resilience4j
- Create: `Resilience4jConfig.java`
- Modify: 调用 HTTP 的代码添加 @Retryable/@CircuitBreaker

**Step 1: pom.xml 添加**
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**Step 2: application.yml 添加**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      httpCall:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 5
  retry:
    instances:
      httpCall:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
```

**Step 3: 在 KafkaConsumerService 或 RestTemplate 调用处添加注解**
```java
@CircuitBreaker(name = "httpCall", fallbackMethod = "fallback")
@Retry(name = "httpCall")
public void pushToRuyi(EventPayload payload) { ... }
```

---

### 任务 3.2: Spring Cloud Gateway 限流
**Objective**: 添加 API 限流防止刷接口
**Files**:
- Create: `RateLimitFilter.java`

**Step 1: 创建限流拦截器**
```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientId = request.getHeader("X-Client-Id");
        // 使用 Redis 或 ConcurrentHashMap 计数
        // 超过限制返回 429 Too Many Requests
    }
}
```

---

## 阶段四: API 品质 (4/5)

### 任务 4.1: 全局异常处理
**Objective**: 统一 API 错误响应格式
**Files**:
- Create: `ruoyi-framework/src/main/java/com/ruoyi/framework/web/exception/GlobalExceptionHandler.java`

**Step 1: 创建 GlobalExceptionHandler**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        return Result.error(400, e.getBindingResult().getFieldError().getDefaultMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
```

**Step 2: 创建 Result 统一响应类**
```java
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data, System.currentTimeMillis());
    }
    
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }
}
```

---

### 任务 4.2: 请求日志拦截器
**Objective**: 记录每个请求的入参、出参，耗时，状态码
**Files**:
- Create: `ruoyi-framework/src/main/java/com/ruoyi/framework/web/interceptor/RequestLogInterceptor.java`
- Modify: `WebMvcConfig.java` — 注册拦截器

**Step 1: RequestLogInterceptor**
```java
@Component
public class RequestLogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long start = System.currentTimeMillis();
        request.setAttribute("start", start);
        log.info("[{}] {} {} started", request.getMethod(), request.getRequestURI(), JSON.toJSONString(request.getParameterMap()));
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long start = (Long) request.getAttribute("start");
        long duration = System.currentTimeMillis() - start;
        log.info("[{}] {} completed with status {} in {}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
    }
}
```

---

### 任务 4.3: API 版本管理
**Objective**: URL 版本管理 v1/v2
**Files**:
- Modify: Controller 上的 @RequestMapping 注解

**Step 1**: 将所有 API Controller 的 @RequestMapping 改为 `/api/v1/xxx` 格式

---

## 阶段五: 代码质量 (5/5)

### 任务 5.1: 提升 Java 单元测试覆盖率
**Objective**: 当前只有 2 个测试文件，覆盖率几乎为 0。添加核心业务测试
**Files**:
- Modify: `ruoyi-biz/src/test/java/com/ruoyi/biz/service/AlarmServiceImplTest.java`
- Create: `ruoyi-biz/src/test/java/com/ruoyi/biz/service/AiEventServiceTest.java`
- Create: `ruoyi-biz/src/test/java/com/ruoyi/biz/kafka/KafkaConsumerServiceTest.java` (增强)

**Step 1: 为 AlarmServiceImpl 添加测试**
```java
@ExtendWith(MockitoExtension.class)
class AlarmServiceImplTest {
    @Mock
    private AlarmMapper alarmMapper;
    
    @InjectMocks
    private AlarmServiceImpl alarmService;
    
    @Test
    void selectPageAlarmListWithDetails_shouldReturnPagedResults() {
        // Given
        IPage<AlarmVo> page = new Page<>(1, 10);
        // When
        IPage<AlarmVo> result = alarmService.selectPageAlarmListWithDetails(page, null);
        // Then
        assertNotNull(result);
    }
}
```

---

### 任务 5.2: commitlint 提交规范
**Objective**: 强制 commit message 格式
**Files**:
- Create: `.commitlintrc.json`

```json
{
  "extends": ["@commitlint/config-conventional"],
  "rules": {
    "type-enum": [2, "always", ["feat", "fix", "refactor", "chore", "docs", "test", "perf", "ci"]]
  }
}
```

---

## 执行顺序

```
阶段一(基础设施) → 阶段二(可观测性) → 阶段三(弹性) → 阶段四(API品质) → 阶段五(代码质量)
```

**每个阶段通过 delegate_task 并行执行多个任务，每个任务由独立的 Claude Code subagent 处理。**
