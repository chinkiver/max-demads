<!-- AGENTS.md — 需求管理平台（max-demands） -->

# AGENTS.md — 需求管理平台（max-demands）

> 本文件面向 AI 编码助手。阅读前默认不了解本项目，因此下文尽量说明背景、结构、构建方式与开发约定。
> 项目主要注释与文档语言为中文，故本文件以中文撰写。
> 内容基于对当前代码库的实际探查，若代码与本文件出现偏差，请以代码为准并同步更新本文件。

## Agent skills

### Issue tracker

Issues 和 PRDs 作为本地 markdown 文件存放在 `.scratch/` 中。详见 `docs/agents/issue-tracker.md`。

### Triage labels

使用默认的五个 canonical triage roles：`needs-triage`、`needs-info`、`ready-for-agent`、`ready-for-human`、`wontfix`。详见 `docs/agents/triage-labels.md`。

### Domain docs

Single-context repo。领域上下文见根目录 `CONTEXT.md`，架构决策见 `docs/adr/`（当前仅有 `README.md`，尚无具体 ADR）。领域术语（Glossary）以 `CONTEXT.md` 为准，命名产物时请复用其中定义的术语。详见 `docs/agents/domain.md`。

---

## 1. 项目概述

`max-demands`（Max 需求管理系统）是一个面向企业研发管理、**内网部署**的**需求全生命周期管理 Web 应用**，覆盖业务需求、产品子需求、开发分支、验证分支、投产批次以及应用系统之间的关联关系。

核心功能模块：

| 模块 | 说明 |
|------|------|
| 业务需求 BizRequirement | 需求编码、名称、种类、概要、优先级、提出人、负责人、状态、关联投产批次；支持字段自定义显示与列宽记忆 |
| 产品子需求 ProdRequirement | 编码、名称、概要、开发系统、开发人员、状态，隶属业务需求；创建时可自动创建开发分支或关联已有分支 |
| 开发分支 DevBranch | 分支名、关联系统、状态、关联验证分支，可被多个产品需求关联 |
| 验证分支 VerifyBranch | 分支名、关联系统、状态、关联投产批次；以关系树展示 |
| 投产批次 Batch | 批次类型、批次日期、状态，支持按月份自动生成；过期批次自动完成 |
| 应用系统 AppSystem | 系统名称、负责人、系统描述、归属业务部门，重复名称校验 |
| 数据字典 Dict | 系统级枚举（需求种类、优先级、状态、分支状态、批次类型等），带缓存 |
| 用户权限 RBAC | 三角色：管理员（admin）、需求分配员（demand_assign）、普通用户（user）；菜单级 + 按钮级权限 |
| 需求全览 / 已投产需求 | 树形展示 业务需求 → 产品需求 → 开发分支 → 验证分支 完整链路 |
| 操作日志 | 通过 `@OperationLog` 注解 + AOP 记录关键操作到 `sys_operation_log` |

关键领域规则（详见 `CONTEXT.md`）：

- 一个业务需求可关联多个产品需求。
- 一个产品需求对应一个开发分支；一个开发分支可被多个产品需求关联。
- 一个开发分支可关联一个验证分支。
- 一个验证分支可关联一个投产批次。

部署形态：Spring Boot 内嵌 Tomcat，打包为单一可执行 JAR（端口 `8183`），前端 Vue 打包产物作为静态资源由 Spring Boot 一并托管。

---

## 2. 技术栈

### 2.1 后端（`pom.xml`，artifact 版本 `1.0.2`）

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK 版本（`maven-compiler-plugin` source/target=17） |
| Spring Boot | 3.1.12 | 父 POM `spring-boot-starter-parent` |
| Spring Security | 6.x | 认证与权限控制（starter-security） |
| Spring Validation / AOP / Cache | 随 Boot | 参数校验、操作日志切面、字典与权限缓存 |
| MyBatis-Plus | 3.5.6 | ORM 框架（`mybatis-plus-boot-starter`） |
| MySQL Connector/J | 随 Boot | 数据库驱动（runtime scope） |
| HikariCP | 默认 | 连接池 |
| JJWT | 0.12.5 | JWT 无状态登录令牌（api / impl / jackson 三件套） |
| Knife4j | 4.5.0 | API 文档（`knife4j-openapi3-jakarta-spring-boot-starter`） |
| Lombok | 随 Boot | 简化实体类代码，打包时 `spring-boot-maven-plugin` 排除 |
| Maven | 3.8+ | 构建工具 |

测试依赖：`spring-boot-starter-test`、`spring-security-test`（均为 test scope）。

### 2.2 前端（`max-demands-ui/package.json`，版本 `1.0.0`，`type: module`）

| 组件 | 版本 | 说明 |
|------|------|------|
| Vue | ^3.4.21 | 前端框架，Composition API + `<script setup>` |
| Element Plus | ^2.7.0 | UI 组件库，`main.js` 中全局注册并加载 `zh-cn` 语言包 |
| @element-plus/icons-vue | ^2.3.1 | 图标库，全局批量注册所有图标组件 |
| Vue Router | ^4.3.0 | 路由管理（hash 模式） |
| Pinia | ^2.1.7 | 状态管理 |
| Axios | ^1.6.8 | HTTP 客户端 |
| Vite | ^6.0.0 | 构建工具（`@vitejs/plugin-vue` ^5.0.4） |

### 2.3 部署架构

```
用户浏览器（内网）
        │
        ▼
Spring Boot（端口 8183）
- 内嵌 Tomcat
- 托管前端静态资源（Vue 打包产物 → src/main/resources/static）
- 提供 REST API（/api/*）
        │
        ▼
MySQL 8.0（端口 3306）
```

---

## 3. 项目结构

```
max-demands/
├── pom.xml                                    # Maven 配置（Spring Boot 父 POM）
├── AGENTS.md / CLAUDE.md / CONTEXT.md / README.md   # 文档（本文件、Claude 指南、领域上下文、用户文档）
├── src/
│   ├── main/
│   │   ├── java/com/maxdemands/
│   │   │   ├── MaxDemandsApplication.java     # Spring Boot 启动类
│   │   │   ├── annotation/                    # @OperationLog 自定义注解
│   │   │   ├── config/                        # MybatisPlusConfig、CacheConfig、WebConfig
│   │   │   ├── controller/                    # 11 个 REST 控制器
│   │   │   ├── service/ + service/impl/       # 9 个服务接口及其实现
│   │   │   ├── mapper/                         # 13 个 MyBatis-Plus Mapper 接口
│   │   │   ├── entity/                         # 15 个实体（BaseEntity/BaseSoftDeleteEntity + 业务/系统表）
│   │   │   ├── dto/                            # 请求 DTO（Login、UserDTO、ProdRequirementDTO 等）
│   │   │   ├── vo/                             # 响应 VO（LoginVO、BizRequirementVO、Overview/Tree 等）
│   │   │   ├── security/                       # SecurityConfig、JwtAuthenticationFilter、
│   │   │   │                                   #   UserDetailsServiceImpl、PermissionService(@ss)
│   │   │   ├── common/
│   │   │   │   ├── exception/                  # BusinessException + GlobalExceptionHandler
│   │   │   │   ├── result/                     # 统一响应包装 Result<T>
│   │   │   │   ├── aop/                        # OperationLogAspect（操作日志切面）
│   │   │   │   └── interceptor/                # UserIdInterceptor（从 JWT 提取 userId）
│   │   │   └── util/                           # JwtTokenProvider
│   │   └── resources/
│   │       ├── application.yml                 # 主配置（默认 active profile = prod）
│   │       ├── application-dev.yml             # 开发环境数据源
│   │       ├── application-prod.yml            # 生产环境数据源 + 关闭 SQL 日志
│   │       ├── logback-spring.xml              # 日志配置
│   │       ├── db/init.sql                     # 数据库初始化脚本（建表 + 初始数据）
│   │       └── static/                         # 前端打包产物（Vite 构建输出）
│   └── test/java/com/maxdemands/               # 测试目录（当前为空）
├── max-demands-ui/                             # Vue3 前端项目
│   ├── package.json / vite.config.js / index.html
│   └── src/
│       ├── main.js                             # 入口（全局注册 Element Plus + 图标 + zh-cn）
│       ├── App.vue
│       ├── api/request.js                      # Axios 封装（baseURL=/api）
│       ├── components/QuickSelect.vue          # 通用组件
│       ├── composables/                        # useColumnWidth.js、useInputHistory.js（列宽/输入历史）
│       ├── router/index.js                     # Vue Router（createWebHashHistory + 守卫）
│       ├── stores/                             # Pinia：auth.js（token/用户信息）、dict.js（字典）
│       ├── styles/theme.css                    # 全局样式
│       └── views/                              # 页面视图（见 §5.2）
├── scripts/                                    # 构建与部署脚本
│   ├── build.ps1 / build.bat                   # Windows 一键构建
│   ├── deploy.conf / deploy.sh                 # 本地构建 + 远程部署（Linux）
│   └── remote/                                 # 服务器端 start.sh / stop.sh / restart.sh
├── docs/                                       # 迁移 SQL + agent/领域文档
│   ├── adr/                                    # 架构决策记录（目前仅 README.md）
│   ├── agents/                                 # issue-tracker.md、triage-labels.md、domain.md
│   └── *.sql                                   # 权限/菜单/批次等增量迁移脚本
├── logs/                                       # 运行日志（max-demands.log）
└── target/                                     # Maven 构建产物（max-demands-1.0.2.jar）
```

> 说明：`src/test` 目录当前为空，尚无任何测试类。`application.yml` 配置了 `mapper-locations: classpath*:mapper/**/*.xml`，但项目当前没有任何 XML Mapper，所有自定义 SQL 均通过 Mapper 接口注解实现。

---

## 4. 构建与运行

### 4.1 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8.0
- Node.js 18+（前端开发/构建）

### 4.2 数据库初始化

创建数据库并执行初始化脚本 `src/main/resources/db/init.sql`：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS max_demands CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p max_demands < src/main/resources/db/init.sql
```

脚本会创建全部业务表与 RBAC 表（`sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`、`sys_dict`、`sys_operation_log`、`app_system`、`batch`、`biz_requirement`、`prod_requirement`、`dev_branch`、`verify_branch`），插入初始角色/权限/字典数据，并初始化默认管理员账号：`admin` / `admin123`。

若从旧版本升级，按需顺序执行 `docs/` 下的迁移脚本（升级前请先备份数据库）：`dashboard-permission-migration.sql`、`biz-requirement-overview-permission-migration.sql`、`app-system-unique-name-migration.sql`、`menu-permission-migration.sql`、`batch-2026.sql`。

### 4.3 后端构建与启动

```bash
mvn clean compile          # 编译
mvn spring-boot:run        # 本地运行
mvn clean package          # 打包（需先构建前端，产物含静态资源）
java -jar target/max-demands-1.0.2.jar   # 运行 JAR
```

后端默认端口：`8183`。`mvn clean package -DskipTests` 可跳过测试打包。

### 4.4 前端开发

```bash
cd max-demands-ui
npm install
npm run dev        # 开发服务器（端口 3000，代理 /api → http://localhost:8183）
npm run build      # 生产构建（产物输出到 ../src/main/resources/static，emptyOutDir=true）
npm run preview    # 预览生产构建
```

`vite.config.js` 关键配置：路径别名 `@ → src`；开发代理 `/api` → `http://localhost:8183`；构建输出 `../src/main/resources/static`。

### 4.5 完整打包流程

先构建前端再打包后端，前端产物即会嵌入 JAR：

```bash
cd max-demands-ui && npm run build && cd ..
mvn clean package -DskipTests
```

**Windows 一键构建**：`scripts/build.ps1`（PowerShell，默认 profile=prod）或双击 `scripts/build.bat`，脚本会依次 `npm install && npm run build` → `mvn clean package -DskipTests` → 校验 `target/max-demands-*.jar`。

> ⚠️ 已知不一致：`README.md`、`CLAUDE.md` 与 `scripts/deploy.sh` 都引用了 `./scripts/build.sh`，但该文件当前**不存在**。在类 Unix 环境请直接使用上面的 npm + mvn 命令，或先补齐 `build.sh` 再运行 `deploy.sh`。

### 4.6 部署到 Linux 服务器

`scripts/deploy.sh`（本地构建 + 上传 + 远程重启）：先在 `scripts/deploy.conf` 中填写 `SERVER_HOST`、`SERVER_USER`、`SERVER_PORT`、`DEPLOY_DIR`、`JAR_NAME`、`PROFILE`，然后执行 `./scripts/deploy.sh`。脚本调用 `scripts/remote/` 下的 `start.sh`/`stop.sh`/`restart.sh` 管理服务，服务器端以 `java -jar <jar> --spring.profiles.active=<profile>` 启动，日志写入 `logs/max-demands.log`。

> ⚠️ 已知问题：`scripts/deploy.conf` 中 `SERVER_PORT` 被定义了两次（第一次为 SSH 端口 22，随后被应用端口 8183 覆盖），实际会导致 SSH 连接使用 8183 而失败。部署前请将该变量拆分为 `SSH_PORT` 与 `APP_PORT` 并同步修改 `deploy.sh`。

服务器环境要求：JDK 17、MySQL 8.0、Nginx（可选反向代理）。

---

## 5. 代码风格与开发约定

### 5.1 后端约定

- **统一响应**：所有 Controller 返回 `com.maxdemands.common.result.Result<T>`，字段为 `code`/`message`/`data`/`timestamp`。
  - 成功：`Result.success()` / `Result.success(data)`，`code=200`，`message="操作成功"`
  - 失败：`Result.error(message)`（默认 `code=500`）或 `Result.error(code, message)`（自定义如 401/403）
- **分层职责**：
  - `Controller`：参数校验、调用 Service、返回 `Result`；权限注解写在方法上
  - `Service`/`impl`：业务逻辑，复杂逻辑拆分为私有方法（如 `BatchServiceImpl.autoGenerate` 按月份自动生成批次、`ProdRequirementServiceImpl` 级联处理分支）
  - `Mapper`：继承 `BaseMapper<T>`，简单 CRUD 无需 XML；自定义 SQL 用 `@Select` 等**注解写在 Mapper 接口**中（例如 `UserMapper.selectPermissionsByUserId`）。目前项目**没有任何 XML Mapper**。
- **事务**：涉及多表写操作的 Service 方法加 `@Transactional`（现用于 `BatchServiceImpl`、`ProdRequirementServiceImpl`、`RoleServiceImpl`、`UserController` 中的部分写操作等）。
- **异常**：业务异常抛 `BusinessException`，统一由 `GlobalExceptionHandler` 处理。
- **实体类**：
  - `BaseEntity`：`id`（`IdType.AUTO`）、`createTime`（插入填充）、`updateTime`（插入/更新填充）
  - `BaseSoftDeleteEntity extends BaseEntity`：追加 `deleted`（`@TableLogic` 逻辑删除，插入填充 0）。**业务表继承它，系统表直接继承 `BaseEntity`。**
  - 时间与逻辑删除字段由 `MybatisPlusConfig`（实现 `MetaObjectHandler`）自动填充。
- **操作日志**：在需要审计的方法上加 `@OperationLog(value=..., module=...)`，由 `OperationLogAspect` 切面统一记录到 `sys_operation_log`。
- **缓存**：`CacheConfig` + Spring Cache。字典、用户权限/角色列表通过 `@Cacheable` 缓存（见 `PermissionService`），权限/角色变更时用 `@CacheEvict` 清理。
- **Lombok**：实体类使用 `@Data`；配置类/服务/控制器使用 `@RequiredArgsConstructor` 做构造器注入。

### 5.2 前端约定

- 一律使用 **Composition API + `<script setup>`**。
- API 调用统一走 `max-demands-ui/src/api/request.js`：
  - `baseURL = '/api'`，`timeout = 10000`
  - 请求拦截器自动附加 `Authorization: Bearer <token>`（token 存 `localStorage`）
  - 响应拦截器：`data.code !== 200` 时 `ElMessage.error`；`code===401` 或 HTTP 401 时清除 token 并弹框跳转 `/login`
- 路由 `router/index.js`：`createWebHashHistory`，页面懒加载；`router.beforeEach` 中用 `authStore.isLoggedIn()` 校验登录态；主布局 `LayoutView.vue` 下挂载各业务页面。
  - 视图目录：`views/` 根下 `LoginView`/`LayoutView`/`DashboardView`；`biz-requirement/`（List、Overview、Completed）、`prod-requirement/`、`branch/`（DevBranchList、VerifyBranchList）、`batch/`、`app-system/`、`system/`（UserManage、RoleManage、DictManage）。
- 状态管理用 Pinia：`stores/auth.js`（token + userInfo，持久化到 `localStorage`）、`stores/dict.js`（数据字典）。
- 复用逻辑放 `composables/`：`useColumnWidth.js`（表格列宽记忆）、`useInputHistory.js`（输入历史）。
- UI 使用 Element Plus（`main.js` 中 `app.use(ElementPlus, { locale: zhCn })`，并全局注册全部 `@element-plus/icons-vue` 图标）。
- **内网部署要求：所有静态资源必须本地化**，不依赖 CDN，Element Plus 从 `node_modules` 引入。

### 5.3 命名约定

- Java 包名：`com.maxdemands.xxx`
- 数据库表：业务表小写下划线（`biz_requirement`、`dev_branch`），系统/RBAC 表前缀 `sys_`
- REST API 路径：全小写、单词用 `-` 连接、`/api` 前缀，如 `/api/biz-requirement`
- Vue 文件：大驼峰，如 `BizRequirementList.vue`
- **权限编码目前命名不统一**：多数为冒号分段（`biz:requirement:list`、`sys:user:add`、`batch:edit`），但开发/验证分支相关权限用了下划线（`dev_branch:list`、`verify_branch:add`）。新增权限时请与所属模块现有编码保持一致。

---

## 6. 安全架构与认证授权

- **无状态 JWT 认证**：`SessionCreationPolicy.STATELESS` + 关闭 CSRF；`JwtTokenProvider`（JJWT 0.12.5）签发/校验令牌，有效期默认 7 天。
- **过滤器与拦截器**：`JwtAuthenticationFilter` 校验 `Bearer` 令牌并写入 `SecurityContext`；`UserIdInterceptor` 从 JWT 提取 `userId`（排除登录/注册接口）。
- **用户加载**：`UserDetailsServiceImpl` 以**用户 ID 作为 principal 用户名**，把角色映射为 `ROLE_<roleCode>` 权限、把权限编码映射为普通 authority；`status==0` 视为账号锁定。
- **RBAC**：用户 ↔ 角色 ↔ 权限 多对多，经 `sys_user_role`、`sys_role_permission` 关联；三角色：管理员、需求分配员、普通用户。
- **接口鉴权（重要，与旧文档不同）**：控制器实际使用 Spring Security 原生 SpEL —— 权限用 `@PreAuthorize("hasAuthority('perm:code')")`，角色用 `@PreAuthorize("hasRole('xxx')")`（依赖 `ROLE_` 前缀）。
  - 项目里存在 `@Service("ss")` 的 `PermissionService`，提供 `hasPermi`/`hasRole`，可通过 `@ss.hasPermi('...')` 调用并带缓存，但**当前控制器几乎均直接用 `hasAuthority`，`@ss.` 形式尚未在注解中实际使用**。新增接口请沿用 `hasAuthority` 风格，除非有意迁移到 `@ss`。
- **匿名放行**：`/api/auth/login`、`/api/auth/register`、`OPTIONS /api/**`、Knife4j/Swagger（`/doc.html`、`/webjars/**`、`/swagger-resources/**`、`/v3/api-docs/**`）、前端静态资源（`/`、`/index.html`、`/assets/**`、常见 js/css/图片/字体后缀）；其余请求全部需认证。
- **CORS**：`WebConfig` 中 `allowedOriginPatterns("*")` + `allowCredentials(true)`，仅为开发便利，生产应收紧为具体域名。
- **异常响应**：未认证返回 `Result.error(401, ...)`，权限不足返回 `Result.error(403, ...)`。

### 安全注意事项（需重点关注）

- **明文凭据入库**：`application-dev.yml` 与 `application-prod.yml` 目前含**硬编码的数据库地址与密码**。生产部署应改为环境变量/外部配置，切勿再提交明文。
- **JWT 密钥硬编码**：`application.yml` 的 `jwt.secret` 为固定字符串，生产必须替换为复杂随机值并外部化。
- **默认账号**：`admin/admin123`，首次部署后应立即修改。
- **SQL 日志**：`application.yml`（dev）用 `StdOutImpl` 打印 SQL；`application-prod.yml` 已切换为 `NoLoggingImpl` 关闭。
- **密码存储**：`BCryptPasswordEncoder`，禁止明文。
- **文件上传**：multipart 限制 100MB。
- **权限最小化**：新增接口务必配置 `@PreAuthorize`，避免默认放行。

---

## 7. 关键配置说明

### 7.1 `application.yml`（主配置）

```yaml
server:
  port: 8183
spring:
  profiles:
    active: prod          # 默认激活生产 profile（注意：不是 dev）
  application:
    name: max-demands
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl   # SQL 日志（prod 中被覆盖关闭）
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:mapper/**/*.xml
jwt:
  secret: maxDemandsSecretKey2024ForJwtTokenGenerationAndValidation
  expiration: 604800000   # 7 天
```

### 7.2 `application-dev.yml` / `application-prod.yml`

均只覆盖数据源（HikariCP：`minimum-idle=5`、`maximum-pool-size=20` 等）。`prod` 额外将 MyBatis-Plus 日志切为 `NoLoggingImpl`。由于默认 profile 为 `prod`，本地开发若要用 dev 数据源，需显式指定 `--spring.profiles.active=dev` 或修改 `application.yml`。

---

## 8. API 与文档

- 启动后访问 Knife4j 文档：`http://localhost:8183/doc.html`
- 所有接口前缀 `/api`，主要模块路径：
  - 认证：`/api/auth/login`、`/api/auth/info`
  - 业务需求：`/api/biz-requirement`（含 `/overview`、`/overview/completed`、`/{id}/prod-requirements`）
  - 产品子需求：`/api/prod-requirement`
  - 投产批次：`/api/batch`、`/api/batch/auto-generate`（按月份自动生成）、`/api/batch/{id}/requirements`
  - 开发分支：`/api/dev-branch`；验证分支：`/api/verify-branch`
  - 应用系统：`/api/app-system`；字典：`/api/dict`
  - 用户/角色/权限：`/api/user`、`/api/role`、`/api/permission`

---

## 9. 测试

- `src/test` 目录目前**为空**，尚无单元测试或集成测试。
- 依赖已就绪：`spring-boot-starter-test`、`spring-security-test`。
- 建议：ServiceImpl 复杂逻辑（批次自动生成、级联删除）用 JUnit 5 + Mockito；Controller 用 `@SpringBootTest` + `@AutoConfigureMockMvc`；权限用 `@WithMockUser`。
- 运行测试：`mvn test`；运行单个测试：`mvn test -Dtest=ClassName#methodName`。

---

## 10. 常见问题与排障

| 问题 | 可能原因 | 处理建议 |
|------|---------|---------|
| 启动报数据库连接失败 | MySQL 未启动 / profile 指向的数据源不可达 | 默认 profile 为 `prod`，检查 `application-prod.yml`（或改用 `--spring.profiles.active=dev`） |
| 前端调用 API 报 404 | 未正确代理到后端 | 确认 Vite dev server 已启动且后端端口为 8183 |
| 权限不足 403 | 用户角色未分配对应权限 | 检查 `sys_user_role`、`sys_role_permission`；权限变更后缓存需失效 |
| 表不存在 | 未执行 init.sql | 手动执行数据库初始化脚本 |
| `deploy.sh` 报找不到 `build.sh` | `scripts/build.sh` 不存在 | 手动执行 `npm run build` + `mvn package`，或补齐 `build.sh` |
| `deploy.sh` 连接服务器失败 | `deploy.conf` 中 `SERVER_PORT` 被重复定义 | 拆分为 `SSH_PORT` 与 `APP_PORT` 并同步修改脚本 |

---

## 11. 扩展建议

- 补充单元测试与集成测试（当前测试目录为空）。
- 将数据库凭据、JWT 密钥外部化（环境变量/配置中心），移除仓库内明文口令。
- 补齐 `scripts/build.sh` 以消除文档与部署脚本的引用缺口。
- 修复 `scripts/deploy.conf` 中 `SERVER_PORT` 重复定义的问题。
- 生产环境收紧 CORS 到具体域名，启用 HTTPS。
- 统一权限编码命名（冒号 vs 下划线）。
- 考虑批次到期提醒等定时任务与数据库备份策略。
