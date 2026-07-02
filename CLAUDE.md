# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Max 需求管理系统 — a full-lifecycle enterprise requirements management platform covering business requirements, product requirements, development/verification branches, deployment batches, and application systems.

- **Backend**: Spring Boot 3.1.12 + Spring Security 6 + MyBatis-Plus 3.5.6 + JDK 17
- **Frontend**: Vue 3.4 + Element Plus 2.7 + Vite 6 + Pinia
- **Database**: MySQL 8.0
- **Backend runs on**: `http://localhost:8183`
- **Frontend dev server**: `http://localhost:3000` (proxies `/api` to backend)

## Build Commands

```bash
# Backend compile (skip tests)
mvn clean compile -DskipTests

# Backend run locally
mvn spring-boot:run

# Frontend install + dev
cd max-demands-ui && npm install && npm run dev

# Frontend production build (outputs to src/main/resources/static)
cd max-demands-ui && npm run build

# One-liner production build (Linux/macOS/Git Bash)
./scripts/build.sh

# One-liner production build (Windows PowerShell)
.\scripts\build.ps1

# Run a single test
mvn test -Dtest=ClassName#methodName
```

## Architecture

### Backend Package Structure

```
com.maxdemands/
├── controller/     # REST controllers (Auth, User, Role, Permission, AppSystem,
│                   # ProdRequirement, DevBranch, VerifyBranch, Batch, BizRequirement, Dict)
├── service/        # Service interfaces + impl/
├── mapper/         # MyBatis-Plus mappers
├── entity/         # JPA/MyBatis-Plus entities
├── dto/, vo/       # Data transfer objects, view objects
├── security/       # JWT filter, UserDetailsService, PermissionService, SecurityConfig
├── common/
│   ├── result/     # Unified API response wrapper (Result)
│   ├── exception/  # BusinessException + GlobalExceptionHandler
│   └── aop/        # OperationLogAspect (AOP logging)
├── annotation/     # @OperationLog
├── config/         # MybatisPlusConfig, CacheConfig, WebConfig
├── util/           # JwtTokenProvider
└── common/interceptor/  # UserIdInterceptor (extracts userId from JWT into request)
```

### Entity Inheritance

- `BaseEntity` — id, createTime, updateTime
- `BaseSoftDeleteEntity extends BaseEntity` — adds `deleted` (logic delete, value=1). **All business tables extend this.**
- System tables (Role, Permission, Dict, AppSystem, User) extend `BaseEntity` directly.

### Security Architecture

- JWT-based stateless authentication (JJWT 0.12.5)
- `JwtAuthenticationFilter` intercepts requests, validates Bearer token
- `UserIdInterceptor` extracts `userId` from JWT and sets it on the request — controllers access it via `@RequestAttribute`
- RBAC: roles ↔ permissions many-to-many via `UserRole`, `RolePermission` join tables
- Password: BCrypt via `PasswordEncoder` bean
- Public endpoints: `/api/auth/login`, `/api/auth/register`, `/doc.html`, `/swagger-resources/**`, static assets
- All other endpoints require authentication

### Frontend Architecture

```
max-demands-ui/src/
├── api/request.js       # Axios instance, Bearer token injection, 401 → /login redirect
├── stores/              # Pinia stores (auth.js, dict.js)
├── views/               # Vue page components
│   ├── LayoutView.vue   # Main layout
│   ├── LoginView.vue    # Login page
│   ├── DashboardView.vue
│   ├── biz-requirement/ # BizRequirementList, BizRequirementCompleted, BizRequirementOverview
│   ├── prod-requirement/
│   ├── branch/          # DevBranchList, VerifyBranchList
│   ├── batch/
│   ├── app-system/
│   └── system/          # UserManage, RoleManage, DictManage
```

### API Convention

- Backend returns `Result<T>` wrapper: `{ code: 200, message: "...", data: T }`
- Frontend axios interceptor checks `data.code !== 200`, shows `ElMessage.error`, handles 401 redirect
- All JWT-protected requests must include `Authorization: Bearer <token>` header

### Database

- MyBatis-Plus with auto-fill (createTime/updateTime/deleted)
- `mapper-locations: classpath*:mapper/**/*.xml` — XML mappers go here if needed
- Active profile: `prod` (see `application.yml` → `spring.profiles.active: prod`)
- Dev overrides in `application-dev.yml`, prod overrides in `application-prod.yml`

## Key Behaviors to Know

- Dev branch and verify branch are managed as separate entities with a parent-child relationship displayed as a tree
- Batch auto-generation creates batches from business requirements; expired batches are grayed out
- BizRequirementOverview shows the full chain: business requirement → product requirement → dev branch → verify branch
- Operation logs are captured via `@OperationLog` annotation on controller methods, processed by `OperationLogAspect`
- Dict values are cached via Spring Cache for fast lookups