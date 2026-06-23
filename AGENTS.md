# AGENTS.md — 需求管理平台（max-demands）

> 本文件面向 AI 编码助手。阅读前默认不了解本项目，因此下文会尽量说明背景、结构、构建方式与开发约定。
> 项目主要注释与文档语言为中文，故本文件以中文撰写。

## Agent skills

### Issue tracker

Issues 和 PRDs 作为本地 markdown 文件存放在 `.scratch/` 中。详见 `docs/agents/issue-tracker.md`。

### Triage labels

使用默认的五个 canonical triage roles：`needs-triage`、`needs-info`、`ready-for-agent`、`ready-for-human`、`wontfix`。详见 `docs/agents/triage-labels.md`。

### Domain docs

Single-context repo。领域上下文见根目录 `CONTEXT.md`，架构决策见 `docs/adr/`。详见 `docs/agents/domain.md`。


---

## 1. 项目概述

`max-demands` 是一个面向内网部署的**需求管理 Web 应用**，用于管理业务需求、产品子需求、开发分支、验证分支、投产批次以及应用系统之间的关联关系。

核心功能模块：

| 模块 | 说明 |
|------|------|
| 业务需求 | 需求编码、名称、种类、概要、优先级、提出人、负责人、状态、关联投产批次 |
| 产品子需求 | 编码、名称、概要、开发系统、开发人员、状态，隶属业务需求 |
| 开发分支 | 分支名、关联系统、状态、关联验证分支，可被多个产品需求关联 |
| 验证分支 | 分支名、关联系统、状态、关联投产批次 |
| 投产批次 | 种类（日常/周/月/紧急）、批次日期、状态，支持按月份自动生成 |
| 应用系统 | 系统名称、负责人、系统描述 |
| 用户权限 | RBAC 模型，三角色：管理员、需求分配员、普通用户 |

部署形态：Spring Boot 内嵌 Tomcat，打包为单一可执行 JAR（端口 `8183`），前端静态资源由 Spring Boot 一并托管。

---

## 2. 技术栈

### 2.1 后端

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK 版本 |
| Spring Boot | 3.1.12 | 基础框架 |
| Spring Security | 6.x | 认证与权限控制 |
| MyBatis-Plus | 3.5.6 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| HikariCP | 默认 | 连接池 |
| JJWT | 0.12.5 | JWT 无状态登录令牌 |
| Knife4j | 4.5.0 | API 文档（Swagger 增强） |
| Lombok | 默认 | 简化实体类代码 |
| Maven | - | 构建工具 |

### 2.2 前端

| 组件 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.21 | 前端框架，Composition API |
| Element Plus | 2.7.0 | UI 组件库 |
| Vue Router | 4.3.0 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Axios | 1.6.8 | HTTP 客户端 |
| Vite | 5.2.0 | 构建工具 |

### 2.3 部署架构

```
用户浏览器（内网）
        │
        ▼
Spring Boot（端口 8183）
- 内嵌 Tomcat
- 托管前端静态资源（Vue 打包产物）
- 提供 REST API（/api/*）
        │
        ▼
MySQL 8.0（端口 3306）
```

---

## 3. 项目结构

```
max-demands/
├── pom.xml                                    # Maven 配置
├── AGENTS.md                                  # 本文件
├── src/
│   ├── main/
│   │   ├── java/com/maxdemands/
│   │   │   ├── MaxDemandsApplication.java     # Spring Boot 启动类
│   │   │   ├── config/                        # 配置类（MyBatis-Plus、Web/跨域）
│   │   │   ├── controller/                    # REST API 控制器
│   │   │   ├── service/                       # 服务接口
│   │   │   │   └── impl/                      # 服务实现
│   │   │   ├── mapper/                        # MyBatis-Plus Mapper 接口
│   │   │   ├── entity/                        # 数据库实体（对应 sys_* / app / batch 等表）
│   │   │   ├── dto/                           # 请求参数 DTO（如 LoginDTO）
│   │   │   ├── vo/                            # 响应视图对象（如 LoginVO、Result）
│   │   │   ├── security/                      # JWT、Spring Security 配置、权限服务
│   │   │   ├── common/                        # 通用类
│   │   │   │   ├── exception/                 # 业务异常与全局异常处理
│   │   │   │   ├── result/                    # 统一响应包装 Result<T>
│   │   │   │   └── interceptor/               # 拦截器（如 UserIdInterceptor）
│   │   │   └── util/                          # 工具类（如 JwtTokenProvider）
│   │   └── resources/
│   │       ├── application.yml                # 主配置
│   │       ├── application-dev.yml            # 开发环境配置（数据库连接）
│   │       ├── db/init.sql                    # 数据库初始化脚本
│   │       └── static/                        # 前端打包产物（由 Vite 构建输出）
│   └── test/java/com/maxdemands/              # 测试目录（目前为空）
├── max-demands-ui/                            # Vue3 前端项目
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js                            # 入口
│       ├── App.vue
│       ├── api/request.js                     # Axios 封装（baseURL=/api）
│       ├── components/                        # 通用组件（QuickSelect 等）
│       ├── router/index.js                    # Vue Router 配置
│       ├── stores/                            # Pinia Store（auth、dict）
│       └── views/                             # 页面视图
├── docs/superpowers/                          # 设计文档与实施计划
│   ├── specs/2026-06-03-max-demands-design.md
│   └── plans/2026-06-03-max-demands-implementation.md
├── data/                                      # 本地数据文件（mcp-db2-config.mv.db）
├── logs/                                      # 日志文件
└── vo/                                        # 空目录（可忽略）
```

---

## 4. 构建与运行

### 4.1 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0
- Node.js 18+（前端开发/构建）

### 4.2 数据库初始化

1. 启动本地 MySQL。
2. 执行 `src/main/resources/db/init.sql`，脚本会：
   - 创建 `max_demands` 数据库（utf8mb4）
   - 创建所有业务表与 RBAC 表
   - 插入初始角色、权限、字典数据
   - 初始化默认管理员账号：`admin` / `admin123`

开发环境数据库配置（`application-dev.yml`）：

```yaml
url: jdbc:mysql://localhost:3306/max_demands?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
username: root
password: root
```

### 4.3 后端构建与启动

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包（包含前端静态资源）
mvn clean package

# 运行生成的 JAR
java -jar target/max-demands-1.0.0.jar
```

后端默认端口：`8183`

### 4.4 前端开发

```bash
cd max-demands-ui

# 安装依赖
npm install

# 开发服务器（端口 3000，代理到 localhost:8183）
npm run dev

# 构建（产物输出到 ../src/main/resources/static）
npm run build

# 预览生产构建
npm run preview
```

`vite.config.js` 关键配置：
- 开发代理：`/api` → `http://localhost:8183`
- 构建输出：`../src/main/resources/static`

### 4.5 完整打包流程

```bash
cd max-demands-ui && npm run build && cd ..
mvn clean package
```

执行后，前端资源会嵌入 JAR，访问 `http://localhost:8183` 即可。

---

## 5. 代码风格与开发约定

### 5.1 后端约定

- **统一响应**：所有 Controller 返回 `Result<T>`，由 `com.maxdemands.common.result.Result` 包装。
  - 成功：`code = 200`，`message = "操作成功"`
  - 失败：`code = 500` 或自定义错误码（400/401/403）
- **分层职责**：
  - `Controller`：只做参数校验、调用 Service、返回 Result
  - `Service/impl`：处理业务逻辑，复杂逻辑拆分为私有方法
  - `Mapper`：继承 `BaseMapper<T>`，简单 CRUD 不写 XML
- **事务**：涉及多表写操作的 Service 方法加 `@Transactional`
- **异常**：业务异常抛 `BusinessException`，统一由 `GlobalExceptionHandler` 处理
- **实体类**：统一继承 `BaseEntity`，包含 `id`、`createTime`、`updateTime`、`deleted`
  - 逻辑删除字段为 `deleted`，由 MyBatis-Plus `@TableLogic` 自动处理
  - 时间字段由 `MybatisPlusConfig` 自动填充
- **安全**：接口权限使用 `@PreAuthorize("@ss.hasPermi('xxx')")`，角色使用 `@PreAuthorize("@ss.hasRole('xxx')")`
- **Lombok**：实体类使用 `@Data`，配置类/服务使用 `@RequiredArgsConstructor` 做构造器注入

### 5.2 前端约定

- 使用 **Composition API + `<script setup>`** 语法
- API 调用统一封装在 `max-demands-ui/src/api/request.js`
  - `baseURL = '/api'`
  - 请求自动携带 `Authorization: Bearer <token>`
  - 响应统一拦截：非 200 弹 `ElMessage.error`，401 跳转登录页
- 路由使用 `createWebHashHistory`，页面按需懒加载
- 路由守卫在 `router.beforeEach` 中检查登录态
- 状态管理使用 Pinia，`stores/auth.js` 负责 token 与用户信息，`stores/dict.js` 负责数据字典
- UI 组件使用 Element Plus，图标使用 `@element-plus/icons-vue`
- 由于内网部署，**所有静态资源必须本地化**：不依赖 CDN，Element Plus 从 `node_modules` 引入

### 5.3 命名约定

- Java 包名：`com.maxdemands.xxx`
- 数据库表：业务表小写（`biz_requirement`），系统表前缀 `sys_`
- REST API 路径：全小写，单词用 `-` 连接，如 `/api/biz-requirement`
- Vue 文件：大驼峰，如 `BizRequirementList.vue`

---

## 6. 测试

### 6.1 当前状态

- 项目 `src/test` 目录目前**为空**，尚未编写单元测试或集成测试。
- `pom.xml` 已引入 `spring-boot-starter-test` 与 `spring-security-test` 依赖，可直接添加测试。

### 6.2 建议的测试策略

- **单元测试**：对 `ServiceImpl` 中的复杂业务逻辑（如批次自动生成、产品需求级联删除）使用 JUnit 5 + Mockito
- **集成测试**：使用 `@SpringBootTest` 测试 Controller 层，配合 `@AutoConfigureMockMvc` 验证接口
- **安全测试**：使用 `spring-security-test` 的 `@WithMockUser` 验证权限注解

### 6.3 运行测试

```bash
mvn test
```

---

## 7. 安全注意事项

- **JWT 密钥**：`application.yml` 中的 `jwt.secret` 为开发环境硬编码密钥，**生产环境必须更换为复杂随机字符串**
- **默认密码**：数据库脚本初始化了 `admin/admin123`，**首次部署后应立即修改**
- **跨域配置**：`WebConfig` 中 `allowedOriginPatterns("*")` 为开发便利，生产环境应指定具体域名
- **SQL 日志**：`application.yml` 开启了 `StdOutImpl` 打印 SQL，生产环境建议关闭
- **文件上传**：multipart 限制为 100MB，根据实际需求调整
- **密码存储**：使用 `BCryptPasswordEncoder`，禁止明文存储
- **权限最小化**：新增接口应及时配置 `@PreAuthorize`，避免默认放行

---

## 8. 关键配置说明

### 8.1 `application.yml`

```yaml
server:
  port: 8183

spring:
  profiles:
    active: dev

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl   # SQL 日志
    map-underscore-to-camel-case: true                       # 下划线转驼峰
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: maxDemandsSecretKey2024ForJwtTokenGenerationAndValidation
  expiration: 604800000   # 7 天
```

### 8.2 `application-dev.yml`

仅包含开发环境数据源配置，使用 HikariCP 连接池。

---

## 9. API 与文档

- 启动后端后，可访问 Knife4j 文档页面：
  - `http://localhost:8183/doc.html`
- 所有接口前缀为 `/api`
- 主要模块路径：
  - 认证：`/api/auth/login`、`/api/auth/info`
  - 业务需求：`/api/biz-requirement`
  - 产品子需求：`/api/prod-requirement`
  - 投产批次：`/api/batch`、`/api/batch/auto-generate`
  - 开发分支：`/api/dev-branch`
  - 验证分支：`/api/verify-branch`
  - 应用系统：`/api/app-system`
  - 字典：`/api/dict`
  - 用户/角色：`/api/user`、`/api/role`

---

## 10. 常见问题与排障

| 问题 | 可能原因 | 处理建议 |
|------|---------|---------|
| 启动报数据库连接失败 | MySQL 未启动或配置错误 | 检查 `application-dev.yml` 中的 URL、用户名、密码 |
| 前端调用 API 报 404 | 未正确代理到后端 | 确认 Vite dev server 已启动且后端端口为 8183 |
| 权限不足 403 | 用户角色未分配对应权限 | 检查 `sys_user_role`、`sys_role_permission` 数据 |
| 表不存在 | 未执行 init.sql | 手动执行数据库初始化脚本 |

---

## 11. 扩展建议

- 添加操作日志表与 AOP 记录关键操作
- 引入分页查询参数校验（当前依赖 MyBatis-Plus 分页插件）
- 补充单元测试与集成测试
- 生产环境增加 HTTPS、JWT 密钥外部化（环境变量/配置中心）
- 考虑数据库备份策略与定时任务（如批次到期提醒）
