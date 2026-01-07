
# 智慧停车项目开发规范指南

## 一、项目基本信息

### 1. 工作目录
- **操作系统**：Windows 11
- **工作区路径**：`D:\IDEA\smart-parking`

### 2. 技术栈要求

- **JDK版本**：25.0.1
- **构建工具**：Maven
- **主框架**：Spring Boot 3.x
- **前端框架**：Vue 3 + Element Plus
- **核心依赖**：
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `lombok`
  - `ruoyi-common`（若依框架公共模块）

### 3. 项目目录结构

```
smart-parking/
├── RuoYi-SpringBoot3-ElementPlus-master/    # 前端项目
│   ├── src/
│   │   ├── api/                          # API接口定义
│   │   ├── components/                   # Vue组件
│   │   ├── views/                        # 页面视图
│   │   └── utils/                        # 工具类
│   └── vite/
├── RuoYi-SpringBoot3-Pro-master/         # 后端项目
│   ├── ruoyi-admin/                      # 管理模块
│   ├── ruoyi-biz/                        # 业务模块
│   ├── ruoyi-common/                     # 公共模块
│   ├── ruoyi-framework/                  # 框架模块
│   ├── ruoyi-generator/                  # 代码生成器
│   ├── ruoyi-quartz/                     # 定时任务模块
│   ├── ruoyi-system/                     # 系统模块
│   └── smart-parking/                   # 智慧停车模块
│       ├── src/main/java/com/aoshiyue/smartparking/  # 主项目代码
│       └── src/test/java/                              # 测试代码
└── sql/                                   # 数据库脚本
```

## 二、分层架构规范

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库，必须通过 Service 层调用；统一使用 `@RestController` |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 必须通过 Repository 层访问数据库；返回 DTO 而非 Entity（除非必要） |
| **Repository** | 数据库访问与持久化操作             | 继承 `JpaRepository`；使用 `@EntityGraph` 避免 N+1 查询问题     |
| **Entity**     | 映射数据库表结构                   | 不得直接返回给前端（需转换为 DTO）；包名统一为 `entity`         |

### 接口与实现分离

- 所有接口实现类需放在接口所在包下的 `impl` 子包中。
- 示例：
  ```
  com.aoshiyue.smartparking.service
  └── impl
      └── ParkingServiceImpl.java
  ```

## 三、安全与性能规范

### 输入校验

- 使用 `@Valid` 与 JSR-303 校验注解（如 `@NotBlank`, `@Size` 等）
  - 注意：Spring Boot 3.x 中校验注解位于 `jakarta.validation.constraints.*`

- 禁止手动拼接 SQL 字符串，防止 SQL 注入攻击。

### 事务管理

- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

## 四、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `ParkingServiceImpl`  |
| 方法/变量  | lowerCamelCase       | `saveParking()`       |
| 常量       | UPPER_SNAKE_CASE     | `MAX_PARKING_SPACES`  |

### 注释规范

- 所有类、方法、字段需添加 **Javadoc** 注释，使用中文注释。

### 类型命名规范（阿里巴巴风格）

| 后缀 | 用途说明                     | 示例               |
|------|------------------------------|--------------------|
| DTO  | 数据传输对象                 | `ParkingDTO`      |
| DO   | 数据库实体对象               | `ParkingDO`       |
| BO   | 业务逻辑封装对象             | `ParkingBO`       |
| VO   | 视图展示对象                 | `ParkingVO`       |
| Query| 查询参数封装对象             | `ParkingQuery`    |

### 实体类简化工具

- 使用 Lombok 注解替代手动编写 getter/setter/构造方法：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 五、扩展性与日志规范

### 接口优先原则

- 所有业务逻辑通过接口定义（如 `ParkingService`），具体实现放在 `impl` 包中（如 `ParkingServiceImpl`）。

### 日志记录

- 使用 `@Slf4j` 注解代替 `System.out.println`
- 日志级别规范：
  - `ERROR`：系统错误、异常信息
  - `WARN`：警告信息
  - `INFO`：重要业务流程信息
  - `DEBUG`：调试信息

## 六、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |

## 七、智慧停车模块特殊规范

### 1. 停车场管理

- 停车场信息实体：`ParkingLot`
- 车位管理实体：`ParkingSpace`
- 订单管理实体：`ParkingOrder`

### 2. 支付集成

- 支付相关逻辑需单独封装在 `payment` 包下
- 支付回调接口需实现幂等性处理

### 3. 数据统计

- 统计相关接口需添加缓存注解 `@Cacheable`
- 定时统计任务需添加分布式锁注解 `@DistributedLock`

## 八、作者信息

- **代码作者**：aoshiyue
- **创建日期**：2026-01-07
- **最后更新**：2026-01-07
