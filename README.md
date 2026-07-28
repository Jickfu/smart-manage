# Smart Manage

Smart Manage 是一个面向企业管理系统的前后端分离架构项目，重点建设可长期演进的架构内核、工程规范和系统基础能力。

> 项目目前处于架构搭建阶段，接口、数据结构和页面能力仍可能发生调整，尚未发布稳定版本，请勿直接用于生产环境。

## 技术栈

### 后端

- Java 21
- Spring Boot 4
- MyBatis-Plus
- PostgreSQL
- Redis、JetCache
- Sa-Token
- Flyway
- Druid

### 前端

- React 19
- TypeScript
- Vite
- Ant Design 6
- TanStack Query
- Zustand

## 项目结构

```text
smart-manage/
├── db/migration/       # Flyway 数据库结构和必要初始化数据
├── docs/architecture/  # 架构设计与优化计划
├── smart-manage-api/   # Spring Boot 后端
└── smart-manage-web/   # React 前端
```

数据库结构和必要初始化数据以根目录 `db/migration` 下的 Flyway 版本脚本为唯一权威来源。后端构建时会将这些脚本复制到类路径，应用启动时自动执行尚未应用的迁移。

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 22+
- pnpm 11+
- PostgreSQL 16+
- Redis

## 本地启动

### 1. 创建数据库

使用 PostgreSQL 创建空数据库：

```sql
CREATE DATABASE smart_manage;
```

默认开发配置连接：

```text
jdbc:postgresql://localhost:5432/smart_manage
用户名：postgres
密码：postgres
```

这些默认值仅用于本地开发，可通过环境变量覆盖：

| 环境变量 | 说明 |
| --- | --- |
| `SMART_MANAGE_DB_URL` | PostgreSQL JDBC 地址 |
| `SMART_MANAGE_DB_USERNAME` | PostgreSQL 用户名 |
| `SMART_MANAGE_DB_PASSWORD` | PostgreSQL 密码 |
| `SMART_MANAGE_REDIS_HOST` | Redis 地址 |
| `SMART_MANAGE_REDIS_PORT` | Redis 端口 |
| `SMART_MANAGE_REDIS_PASSWORD` | Redis 密码 |
| `SMART_MANAGE_REDIS_DATABASE` | Redis 数据库编号 |

### 2. 启动后端

```bash
cd smart-manage-api
mvn spring-boot:run
```

后端默认地址为 `http://localhost:8080/smart-manage-api`。首次启动会通过 Flyway 创建表结构并写入必要系统数据。

接口文档：

- Swagger UI：`http://localhost:8080/smart-manage-api/swagger-ui.html`
- Scalar：`http://localhost:8080/smart-manage-api/scalar`

开发库初始化完成后的管理员账号：

```text
用户名：administrator
密码：admin
```

上述固定凭据只用于开发和演示环境。生产环境使用 `prod` Profile 启动时，必须通过
`SMART_MANAGE_INITIAL_ADMINISTRATOR_PASSWORD` 提供非 `admin` 的初始管理员密码；
系统仅在数据库仍保留演示密码时执行一次替换，不会在后续重启时覆盖管理员主动修改的密码。

该账号仅用于本地开发和首次初始化，首次登录后应立即修改密码。

### 3. 启动前端

```bash
cd smart-manage-web
pnpm install
pnpm dev
```

前端默认地址为 `http://localhost:8000`，开发服务器会将 `/smart-manage-api` 代理到后端服务。

## 常用命令

### 后端

```bash
cd smart-manage-api
mvn compile
mvn package
```

### 前端

```bash
cd smart-manage-web
pnpm lint
pnpm format:check
pnpm build
```

### Flyway 空库验证

Windows 环境可通过以下脚本创建临时数据库，顺序执行全部迁移并在验证后自动删除临时库：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\db\verify-baseline.ps1
```

## 安全说明

- `application-dev.yml` 中的默认密码和 SM2 密钥仅用于本地开发，均为公开配置。
- 生产环境必须启用 `prod` Profile，并通过环境变量提供数据库、Redis、SM2 等敏感配置。
- 前端权限判断只控制页面能力展示，后端 `@SaCheckPermission` 始终是最终鉴权边界。
- SQL 控制台、脚本控制台和诊断能力等高风险功能还会校验超级管理员身份。

## 参与开发

项目仍在快速演进阶段。提交修改前请先阅读 [AGENTS.md](./AGENTS.md) 和 [企业架构优化计划](./docs/architecture/enterprise-architecture-optimization-plan.md)，并确保相关前后端检查通过。

## 许可证

本项目基于 [Apache License 2.0](./LICENSE) 开源。
