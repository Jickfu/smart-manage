# Smart Manage 文档

本文档集按“当前事实、开发操作、未来提案、历史归档”分区。相同内容只保留一个权威来源，其他位置使用链接引用。

## 当前架构

- [架构总览](./architecture/overview.md)
- [后端架构](./architecture/backend.md)
- [前端架构](./architecture/frontend.md)
- [安全架构](./architecture/security.md)
- [部署与多实例架构](./architecture/deployment.md)
- [功能、菜单与权限模型](./architecture/feature-and-permission.md)
- [数据脱敏架构](./architecture/data-masking.md)
- [数据权限设计](./architecture/data-permission.md)
- [日志数据生命周期](./architecture/log-lifecycle.md)
- [前端页面注册约定](./architecture/page-registration-convention.md)

`architecture/` 只描述当前已经生效的设计和边界，不记录实施计划或历史进度。

## 开发规范

- [环境与配置](./development/configuration.md)
- [质量验证](./development/verification.md)
- [数据库开发](./development/database.md)
- [新增业务聚合检查清单](./development/business-aggregate-checklist.md)

## 领域与模块

领域文档按照 `domains/{领域}/{应用}/{模块}.md` 组织，与前后端代码目录保持一致。

领域文档只维护需要跨实现长期保留的业务事实，包括模块职责、聚合边界、状态与不变量、关键交互、事务与并发语义、稳定集成标识以及安全和对象级授权边界。以下内容不在领域文档中重复维护：

- Flyway 版本、迁移文件名、建表过程和当前基线组成；数据库规则统一引用[数据库开发](./development/database.md)，实际结构以 `db/migration` 为准；
- 权限码、权限前缀及权限与接口的逐项清单；权限事实以代码中的权限常量、Controller 注解和数据库内置目录为准，并由质量门禁校验；
- 可以直接从 Controller、路由注册、实体或配置读取的完整接口、表字段和配置项清单；只有构成稳定跨模块契约的标识才保留；
- 实施进度、临时兼容过程和移除脚本；未来工作进入路线图或提案，历史过程进入归档。

领域文档仍应说明“哪些主体在什么业务条件下可以执行什么动作”等稳定授权语义，但不复制具体权限码。模块涉及高风险能力时，还应保留管理员身份复核、对象级授权和默认拒绝等安全边界。

### 系统领域（sys）

基础应用（base）：

- [附件与对象存储](./domains/sys/base/attachment.md)
- [基础资料](./domains/sys/base/basic-data.md)
- [编号规则](./domains/sys/base/numbering.md)
- [组织管理](./domains/sys/base/organization.md)

监控应用（monitor）：

- [缓存监控](./domains/sys/monitor/cache-and-redis.md)
- [系统监控日志](./domains/sys/monitor/logs.md)
- [运行监控](./domains/sys/monitor/runtime-monitoring.md)
- [脚本控制台](./domains/sys/monitor/script-console.md)
- [慢 SQL 监控](./domains/sys/monitor/slow-sql-monitoring.md)
- [SQL 控制台](./domains/sys/monitor/sql-console.md)
- [线程诊断](./domains/sys/monitor/thread-diagnostics.md)

调度应用（scheduler）：

- [任务调度](./domains/sys/scheduler/job.md)

### 供应链领域（scm）

采购应用（procurement）：

- [采购申请](./domains/scm/procurement/purchase-requisition.md)

模块文档不要求与运行时 Feature 一一对应。新增或修改领域文档时，应按上述边界审查，避免复制由代码、迁移或配置维护的易变清单。

## 计划与提案

- [项目路线图](./roadmap.md)
- [登录保护架构](./architecture/login-protection.md)

`roadmap.md` 只记录已批准但尚未完成的项目级事项。`proposals/` 中的内容尚未生效，不能作为实现依据。

## 历史归档

- [企业级架构优化计划（2026-07）](./archive/enterprise-architecture-optimization-plan-2026-07.md)
- [架构收口报告（2026-07）](./archive/architecture-closure-report-2026-07.md)
- [领域服务审查记录（2026-08）](./archive/reviews-2026-08/sys-domain-service-review-2026-08.md)
- [领域服务审查与整改记录（2026-08）](./archive/reviews-2026-08/sys-domain-service-remediation-2026-08.md)
- [前端审查记录（2026-08）](./archive/reviews-2026-08/web-frontend-review-2026-08.md)

归档只用于追溯，不代表当前架构、规则或项目状态。
