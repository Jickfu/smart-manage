# Smart Manage

Smart Manage 是一个面向企业管理系统的前后端分离项目，重点建设可长期演进的架构内核、工程规范和系统基础能力。

> 项目仍处于架构搭建阶段，尚未发布稳定版本。接口、数据结构和页面能力可能调整，请勿直接用于生产环境。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Java 21、Spring Boot 4、MyBatis-Plus、PostgreSQL、Redis、Sa-Token、Flyway、Druid |
| 前端 | React 19、TypeScript、Vite、Ant Design 6、TanStack Query、Zustand |

## 项目结构

```text
smart-manage/
├── db/migration/       # Flyway 数据库结构和必要初始化数据
├── docs/               # 架构、开发规范、模块和提案
├── smart-manage-api/   # Spring Boot 后端
└── smart-manage-web/   # React 前端
```

数据库结构和必要初始化数据以 `db/migration` 中的 Flyway 迁移为唯一权威来源。

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 22+
- pnpm 11+
- PostgreSQL 16+
- Redis（开发配置默认密码为 `redis123`，数据库编号为 `1`）

## 快速启动

### 1. 创建数据库

```sql
CREATE DATABASE smart_manage;
```

默认开发配置使用 `postgres/postgres` 连接本机 `smart_manage` 数据库，仅用于本地开发。

### 2. 启动后端

```bash
cd smart-manage-api
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080/smart-manage-api`

- Swagger UI：`http://localhost:8080/smart-manage-api/swagger-ui.html`
- Scalar：`http://localhost:8080/smart-manage-api/scalar`

开发环境初始化账号为 `administrator/admin`，只允许用于本地开发和演示。生产环境禁止使用该密码。

### 3. 启动前端

```bash
cd smart-manage-web
pnpm install
pnpm dev
```

前端默认地址：`http://localhost:8000`。开发服务器将 `/smart-manage-api` 代理到后端。

完整配置说明见[环境与配置](./docs/development/configuration.md)。

## 最小验证

```bash
cd smart-manage-api
mvn test
```

```bash
cd smart-manage-web
pnpm lint
pnpm format:check
pnpm test
pnpm build
```

以上为代码修改的快速验证入口；按改动类型执行的完整门槛、组件注册表及真实 PostgreSQL 验证见[质量验证](./docs/development/verification.md#按改动类型选择验证)。纯文档修改不要求执行上述构建和测试。

## 文档

- [文档导航](./docs/README.md)
- [当前架构](./docs/architecture/overview.md)
- [开发与贡献](./CONTRIBUTING.md)
- [项目路线图](./docs/roadmap.md)
- [新增业务聚合检查清单](./docs/development/business-aggregate-checklist.md)

AI 编码代理还必须遵循根目录及对应子项目中的 `AGENTS.md`。

## 安全说明

- 开发配置中的默认密码和密钥均为公开配置，不得用于生产环境。
- 生产环境必须启用 `prod` Profile，并通过环境变量提供敏感配置。
- 前端权限只控制界面能力，后端始终是最终鉴权边界。
- SQL、脚本和诊断等高风险能力还会校验超级管理员身份。

## 许可证

本项目基于 [Apache License 2.0](./LICENSE) 开源。
