
# 开发规范指南
为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、技术栈要求

- **主框架**：Spring Boot 3.5.8
- **语言版本**：Java 17
- **核心依赖**：
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `lombok`
  - `mybatis-plus-spring-boot3-starter`
  - `druid-spring-boot-3-starter`
  - `pagehelper-spring-boot-starter`

## 二、项目目录结构

```
RuoYi-SpringBoot3-Pro-master/
├── ruoyi-admin/                 # Web服务入口模块
│   └── src/main/java/com/ruoyi/
│       ├── interceptor/         # 拦截器
│       └── web/
│           ├── controller/      # 控制器
│           │   ├── common/      # 通用控制器
│           │   ├── monitor/     # 监控控制器
│           │   ├── system/      # 系统控制器
│           │   └── tool/       # 工具控制器
│           └── core/
│               └── config/      # 核心配置
├── ruoyi-biz/                  # 业务模块
│   └── src/main/java/com/ruoyi/biz/
│       ├── controller/         # 业务控制器
│       ├── domain/            # 业务领域对象
│       │   └── excelhandler/  # Excel处理
│       ├── mapper/            # 数据访问层
│       ├── service/           # 业务逻辑层
│       │   └── impl/         # 业务实现
│       └── task/             # 定时任务
├── ruoyi-common/              # 通用工具模块
│   └── src/main/java/com/ruoyi/common/
│       ├── annotation/        # 注解
│       ├── config/           # 配置
│       │   └── serializer/   # 序列化配置
│       ├── constant/         # 常量
│       ├── core/            # 核心组件
│       │   ├── controller/   # 通用控制器
│       │   ├── domain/      # 领域对象
│       │   ├── page/        # 分页
│       │   ├── redis/       # Redis配置
│       │   └── text/       # 文本处理
│       ├── enums/          # 枚举
│       ├── exception/      # 异常处理
│       │   ├── base/       # 基础异常
│       │   ├── file/       # 文件异常
│       │   ├── job/        # 任务异常
│       │   └── user/      # 用户异常
│       ├── filter/         # 过滤器
│       ├── utils/         # 工具类
│       │   ├── ai/        # AI工具
│       │   ├── bean/      # Bean工具
│       │   ├── file/      # 文件工具
│       │   ├── html/      # HTML工具
│       │   ├── http/      # HTTP工具
│       │   ├── ip/        # IP工具
│       │   ├── poi/       # Excel工具
│       │   ├── reflect/    # 反射工具
│       │   ├── sign/      # 签名工具
│       │   ├── spring/    # Spring工具
│       │   ├── sql/       # SQL工具
│       │   ├── uuid/      # UUID工具
│       │   └── wx/        # 微信工具
│       └── xss/          # XSS防护
├── ruoyi-framework/          # 框架核心模块
│   └── src/main/java/com/ruoyi/framework/
│       ├── aspectj/         # AOP切面
│       ├── config/         # 配置
│       │   └── properties/ # 属性配置
│       ├── datasource/     # 数据源配置
│       ├── interceptor/    # 拦截器实现
│       ├── manager/        # 管理器
│       │   └── factory/   # 工厂类
│       ├── security/      # 安全配置
│       │   ├── context/   # 安全上下文
│       │   ├── filter/    # 安全过滤器
│       │   └── handle/    # 安全处理器
│       ├── sms/           # 短信服务
│       └── web/          # Web相关
│           ├── domain/    # Web领域对象
│           ├── exception/ # Web异常
│           └── service/   # Web服务
├── ruoyi-generator/        # 代码生成模块
│   └── src/main/java/com/ruoyi/generator/
│       ├── config/        # 生成配置
│       ├── controller/    # 生成控制器
│       ├── domain/        # 生成领域对象
│       ├── mapper/        # 生成数据访问
│       ├── service/       # 生成服务
│       └── util/         # 生成工具
│           └── resources/mapper/generator/
│           └── resources/vm/  # Velocity模板
│               ├── java/     # Java模板
│               ├── js/       # JavaScript模板
│               ├── sql/      # SQL模板
│               ├── vue/      # Vue模板
│               └── xml/      # XML模板
├── ruoyi-quartz/          # 定时任务模块
│   └── src/main/java/com/ruoyi/quartz/
│       ├── config/        # 任务配置
│       ├── controller/    # 任务控制器
│       ├── domain/        # 任务领域对象
│       ├── mapper/        # 任务数据访问
│       ├── service/       # 任务服务
│       │   └── impl/     # 任务实现
│       ├── task/         # 任务类
│       └── util/        # 任务工具
└── ruoyi-system/         # 系统模块
    └── src/main/java/com/ruoyi/system/
        ├── domain/       # 系统领域对象
        │   └── vo/      # 视图对象
        ├── mapper/      # 系统数据访问
        └── service/     # 系统服务
            └── impl/    # 系统实现
```

## 三、分层架构规范

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库，必须通过 Service 层调用                  |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 必须通过 Repository 层访问数据库；返回 DTO 而非 Entity（除非必要） |
| **Repository** | 数据库访问与持久化操作             | 继承 `JpaRepository`；使用 `@EntityGraph` 避免 N+1 查询问题     |
| **Entity**     | 映射数据库表结构                   | 不得直接返回给前端（需转换为 DTO）；包名统一为 `entity`         |

### 接口与实现分离

- 所有接口实现类需放在接口所在包下的 `impl` 子包中。

## 四、安全与性能规范

### 输入校验

- 使用 `@Valid` 与 JSR-303 校验注解（如 `@NotBlank`, `@Size` 等）
  - 注意：Spring Boot 3.x 中校验注解位于 `jakarta.validation.constraints.*`

- 禁止手动拼接 SQL 字符串，防止 SQL 注入攻击。

### 事务管理

- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

### 数据库连接池配置

- 使用 Druid 连接池，配置如下：
  ```yaml
  spring:
    datasource:
      type: com.alibaba.druid.pool.DruidDataSource
      druid:
        initial-size: 5
        min-idle: 5
        max-active: 20
        max-wait: 60000
        time-between-eviction-runs-millis: 60000
        min-evictable-idle-time-millis: 300000
        validation-query: SELECT 1 FROM DUAL
        test-while-idle: true
        test-on-borrow: false
        test-on-return: false
        pool-prepared-statements: true
        max-pool-prepared-statement-per-connection-size: 20
  ```

### 缓存配置

- 使用 Redis 作为缓存中间件，配置连接池：
  ```yaml
  spring:
    redis:
      host: localhost
      port: 6379
      database: 0
      lettuce:
        pool:
          min-idle: 0
          max-idle: 8
          max-active: 8
          max-wait: -1ms
  ```

## 五、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `UserServiceImpl`     |
| 方法/变量  | lowerCamelCase       | `saveUser()`          |
| 常量       | UPPER_SNAKE_CASE     | `MAX_LOGIN_ATTEMPTS`  |
| 包名       | 小写，点分隔         | `com.ruoyi.system`    |

### 注释规范

- 所有类、方法、字段需添加 **Javadoc** 注释。
- 注释使用中文编写，便于国内团队理解。

### 类型命名规范（阿里巴巴风格）

| 后缀 | 用途说明                     | 示例         |
|------|------------------------------|--------------|
| DTO  | 数据传输对象                 | `UserDTO`    |
| DO   | 数据库实体对象               | `UserDO`     |
| BO   | 业务逻辑封装对象             | `UserBO`     |
| VO   | 视图展示对象                 | `UserVO`     |
| Query| 查询参数封装对象             | `UserQuery`  |
| Excel| Excel处理对象               | `UserExcel`  |

### 实体类简化工具

- 使用 Lombok 注解替代手动编写 getter/setter/构造方法：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 六、扩展性与日志规范

### 接口优先原则

- 所有业务逻辑通过接口定义（如 `UserService`），具体实现放在 `impl` 包中（如 `UserServiceImpl`）。

### 日志记录

- 使用 `@Slf4j` 注解代替 `System.out.println`
- 日志级别使用规范：
  - `DEBUG`：调试信息
  - `INFO`：正常运行信息
  - `WARN`：警告信息
  - `ERROR`：错误信息

### 配置管理

- 所有配置项统一放在 `application.yml` 中
- 敏感信息（如数据库密码）使用环境变量或配置中心管理
- 多环境配置使用 `application-{profile}.yml` 区分

## 七、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |

## 八、开发环境配置

### 用户工作目录

- 工作区路径：`D:\IDEA\smart-parking\RuoYi-SpringBoot3-Pro-master`
- 操作系统：Windows 11
- 当前时间：2026-01-07 16:06:23

### 构建工具

- **Maven**：项目构建和依赖管理
- **Spring Boot Maven Plugin**：用于构建可执行jar包
- **ClassFinal Maven Plugin**：用于代码加密（版本1.4.1）

### SDK版本

- **JDK**：17
- **Spring Boot**：3.5.8
- **Maven**：3.x

### 依赖管理

- 使用父POM统一管理依赖版本（`ruoyi`，版本3.9.1）
- 依赖声明在 `dependencyManagement` 中统一管理
- 使用阿里云Maven镜像仓库加速依赖下载

## 九、代码作者

- **作者**：aoshiyue
- **项目名称**：若依管理系统（RuoYi-SpringBoot3-Pro）
- **版本**：3.9.1
