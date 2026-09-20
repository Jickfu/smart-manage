# Smart Manage 文档

本文档集按架构、开发操作、领域语义和提案分区。相同内容只保留一个权威来源，其他位置使用链接引用。
架构总览可以保留稳定结论的简短摘要，检查清单可以将权威规则转换为验收项；摘要和清单必须链接权威文档，不得独立扩展或改写规则。

## 按任务阅读

生效文档规定应遵守的约束，代码和测试用于核实实现现状。两者不一致时，应区分文档过时与实现缺陷；不得通过修改文档把实现缺陷合法化。

已加载且未变化的规则无需重复读取。无需在每次改动前通读全部文档。先按任务阅读最小必要集合，再跟随文内链接进入安全、部署或领域细节。

| 任务 | 首先阅读 | 按需补充 |
| --- | --- | --- |
| 理解项目整体设计 | [架构总览](./architecture/overview.md) | 对应的前端、后端、安全或部署架构 |
| 修改普通后端代码 | [后端架构](./architecture/backend.md)、[质量验证](./development/verification.md) | 对应领域模块文档 |
| 修改桌面管理端页面或交互 | [前端架构](./architecture/frontend.md)、[前端页面指南](./development/frontend-page-guide.md)、[质量验证](./development/verification.md) | [页面注册约定](./architecture/page-registration-convention.md)、[模块样板目录](./development/module-pattern-catalog.md) |
| 新增或显著扩展业务模块 | [模块开发指南](./development/module-development-guide.md)、[模块样板目录](./development/module-pattern-catalog.md)、对应领域文档 | 新聚合再使用[业务聚合检查清单](./development/business-aggregate-checklist.md) |
| 认证、权限或高风险能力 | [安全架构](./architecture/security.md) | [登录保护](./architecture/login-protection.md)、[功能与权限](./architecture/feature-and-permission.md)、[数据权限](./architecture/data-permission.md)、[数据脱敏](./architecture/data-masking.md) |
| 数据库或迁移 | [数据库开发](./development/database.md)、[质量验证](./development/verification.md) | 日志分区再阅读[日志数据生命周期](./architecture/log-lifecycle.md) |
| 配置、部署或多实例 | [环境与配置](./development/configuration.md)、[部署与多实例架构](./architecture/deployment.md) | 涉及内建监控时阅读[内建监控架构](./architecture/monitoring.md) |

## 当前架构

### 核心架构

- [架构总览](./architecture/overview.md)
- [后端架构](./architecture/backend.md)
- [前端架构](./architecture/frontend.md)
- [前端页面注册约定](./architecture/page-registration-convention.md)

### 安全与访问控制

- [安全架构](./architecture/security.md)
- [登录保护架构](./architecture/login-protection.md)
- [功能、菜单与权限模型](./architecture/feature-and-permission.md)
- [数据脱敏架构](./architecture/data-masking.md)
- [数据权限设计](./architecture/data-permission.md)

### 运行与部署

- [部署与多实例架构](./architecture/deployment.md)
- [日志数据生命周期](./architecture/log-lifecycle.md)
- [内建监控架构](./architecture/monitoring.md)

`architecture/` 只描述当前已经生效的设计和边界，不记录实施计划或历史进度。

## 开发规范

### 环境与质量

- [环境与配置](./development/configuration.md)
- [质量验证](./development/verification.md)
- [数据库开发](./development/database.md)

### 模块开发

- [模块开发指南](./development/module-development-guide.md)
- [前端页面指南](./development/frontend-page-guide.md)
- [模块样板目录](./development/module-pattern-catalog.md)
- [新增业务聚合检查清单](./development/business-aggregate-checklist.md)

## 平台功能与可选领域

平台内置功能文档按照 `docs/platform/{应用}/{模块}.md` 组织，涵盖前后端业务语义和交互；可选领域文档随模块放在 `smart-manage-server/domains/{领域}/docs/{应用}/`。平台底层机制及跨领域约束仍归 `docs/architecture/`。文档路径简化不改变代码中的 `sm.domain.sys` 或前端 `domain/sys`；文件名可以采用可读的连字符形式。

领域文档只维护需要跨实现长期保留的业务事实，包括模块职责、聚合边界、状态与不变量、关键交互、事务与并发语义、稳定集成标识以及安全和对象级授权边界。以下内容不在领域文档中重复维护：

- Flyway 版本、迁移文件名、建表过程和当前基线组成；数据库规则统一引用[数据库开发](./development/database.md)，实际结构以 各模块 `src/main/resources/db/{领域}/migration` 为准；
- 权限码、权限前缀及权限与接口的逐项清单；权限事实以代码中的权限常量、Controller 注解和数据库内置目录为准，并由质量门禁校验；
- 可以直接从 Controller、路由注册、实体或配置读取的完整接口、表字段和配置项清单；只有构成稳定跨模块契约的标识才保留；
- 实施进度、临时兼容过程和移除脚本；未来工作进入路线图或提案，历史过程通过 Git 历史和 Issue 追溯。

领域文档仍应说明“哪些主体在什么业务条件下可以执行什么动作”等稳定授权语义，但不复制具体权限码。模块涉及高风险能力时，还应保留管理员身份复核、对象级授权和默认拒绝等安全边界。

### 平台内置功能（系统领域 sys）

基础应用（base）：

- [附件与对象存储](./platform/base/attachment.md)
- [基础资料](./platform/base/basic-data.md)
- [编号规则](./platform/base/numbering.md)
- [组织管理](./platform/base/organization.md)
- [首页快速发起](./platform/base/home-quick-launch.md)
- [OpenAPI 开放平台](./platform/base/openapi-platform.md)
- [角色管理](./platform/base/role.md)
- [系统参数](./platform/base/system-parameter.md)
- [界面配置](./platform/base/ui-config.md)
- [用户管理](./platform/base/user.md)
- [弱口令管理与固定密码策略](./platform/base/weak-password.md)

监控应用（monitor）：

- [缓存监控](./platform/monitor/cache-and-redis.md)
- [系统监控日志](./platform/monitor/logs.md)
- [运行监控](./platform/monitor/runtime-monitoring.md)
- [脚本控制台](./platform/monitor/script-console.md)
- [慢 SQL 监控](./platform/monitor/slow-sql-monitoring.md)
- [SQL 控制台](./platform/monitor/sql-console.md)
- [线程诊断](./platform/monitor/thread-diagnostics.md)
- [监控告警](./platform/monitor/alerting.md)

调度应用（scheduler）：

- [任务调度](./platform/scheduler/job.md)

消息应用（message）：

- [邮件](./platform/message/email.md)
- [站内消息](./platform/message/inbox.md)

### 演示领域（demo）

采购应用（procurement）：

- [采购申请](../smart-manage-server/domains/demo/docs/procurement/purchase-requisition.md)

桌面管理端的技术基线、页面形态和页签规则只适用于 `smart-manage-web`；其他客户端在实现时另行明确适用规范，不把选型讨论写成当前架构。

模块文档不要求与运行时 Feature 一一对应。新增或修改领域文档时，应按上述边界审查，避免复制由代码、迁移或配置维护的易变清单。

## 计划与提案

- [项目路线图](./roadmap.md)

`roadmap.md` 只记录已批准但尚未完成的项目级事项。`proposals/` 保留方案背景与后续设想，不作为生效规则；已采纳约束以对应架构和领域文档为准。

- [Excel 导入导出方案背景与后续边界](./proposals/excel-import-export.md)
