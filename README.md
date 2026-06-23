# Max 需求管理系统

Max 需求管理系统是一个面向企业研发管理的需求全生命周期管理平台，覆盖业务需求、产品需求、开发分支、验证分支、投产批次及应用系统等核心研发对象。

## 技术栈

- **后端**：Spring Boot 3.1.12 + Spring Security 6 + MyBatis-Plus 3.5.6 + JDK 17
- **前端**：Vue 3.4.21 + Element Plus 2.7.0 + Vite 6.4.3
- **数据库**：MySQL 8.0
- **构建工具**：Maven 3.8+
- **运行时**：Node.js 18+

## 功能特性

- **用户与权限**：基于 RBAC 的角色权限管理，支持菜单级与按钮级权限控制
- **业务需求管理**：业务需求录入、编辑、查看、状态流转、字段自定义显示、列宽记忆
- **产品需求管理**：关联业务需求与系统，开发人员分配，分支关联
- **开发分支 / 验证分支**：分支生命周期管理，分支关系树展示
- **投产批次管理**：批次生成、关联业务需求、过期批次灰显
- **应用系统管理**：系统信息维护，归属业务部门，重复名称校验
- **数据字典管理**：业务需求分类、状态、优先级、分支状态、批次类型等字典维护
- **需求全览 / 已投产需求**：树形展示需求 → 产品需求 → 开发分支 → 验证分支完整链路

## 目录结构

```
max-demands/
├── src/main/java/          # 后端 Java 源码
├── src/main/resources/     # 后端配置与静态资源
│   └── db/init.sql         # 数据库初始化脚本
├── max-demands-ui/         # 前端 Vue 项目
├── docs/                   # 迁移脚本与说明文档
├── pom.xml                 # Maven 配置
└── README.md               # 本文件
```

## 本地开发

### 1. 环境准备

- JDK 17
- Maven 3.8+
- Node.js 18+
- MySQL 8.0

### 2. 数据库初始化

创建数据库并执行初始化脚本：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS max_demands CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p max_demands < src/main/resources/db/init.sql
```

### 3. 后端启动

```bash
mvn clean compile
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8183`。

### 4. 前端启动

```bash
cd max-demands-ui
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:3000`。

### 5. 默认账号

- 用户名：`admin`
- 密码：`admin123`

## 生产构建

### 一键构建（推荐）

Linux / macOS / Git Bash：

```bash
./scripts/build.sh
```

Windows：

```powershell
# PowerShell
.\scripts\build.ps1
```

或双击运行 `scripts/build.bat`。

构建完成后，产物为：

```
target/max-demands-*.jar
```

### 分步构建

```bash
# 1. 构建前端
cd max-demands-ui
npm install
npm run build

# 2. 构建后端 JAR
cd ..
mvn clean package -DskipTests
```

## 部署到 Linux 服务器

### 方式一：本地构建 + 远程部署

1. 修改 `scripts/deploy.conf` 中的服务器信息：
   - `SERVER_HOST`
   - `SERVER_USER`
   - `DEPLOY_DIR`

2. 执行部署脚本：

```bash
./scripts/deploy.sh
```

脚本会自动完成构建、上传、停止旧服务、启动新服务。

### 方式二：直接在服务器上构建部署

1. 将代码上传到服务器
2. 在服务器上执行：

```bash
./scripts/build.sh
./scripts/remote/restart.sh
```

### 服务器环境要求

- JDK 17
- MySQL 8.0
- Nginx（可选，用于反向代理）

## 配置文件

生产环境主要配置位于：

```
src/main/resources/application-prod.yml
```

建议覆盖以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://<数据库地址>:3306/max_demands?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: <用户名>
    password: <密码>

server:
  port: 8183
```

## 数据库迁移

如果从旧版本升级，请按顺序执行 `docs/` 目录下的迁移脚本：

```bash
mysql -u root -p max_demands < docs/dashboard-permission-migration.sql
mysql -u root -p max_demands < docs/biz-requirement-overview-permission-migration.sql
mysql -u root -p max_demands < docs/app-system-unique-name-migration.sql
```

> 每次升级前请备份数据库。

## 常用命令

```bash
# 后端测试编译
mvn clean compile -DskipTests

# 前端构建
cd max-demands-ui && npm run build

# 查看日志
tail -f logs/max-demands.log
```

## 许可证

本项目仅供内部使用。
