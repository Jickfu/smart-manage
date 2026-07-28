# Smart Manage 文档

本文档集按“当前事实、开发操作、未来提案、历史归档”分区。相同内容只保留一个权威来源，其他位置使用链接引用。

## 当前架构

- [架构总览](./architecture/overview.md)
- [后端架构](./architecture/backend.md)
- [前端架构](./architecture/frontend.md)
- [安全架构](./architecture/security.md)
- [前端页面注册约定](./architecture/page-registration-convention.md)

`architecture/` 只描述当前已经生效的设计和边界，不记录实施计划或历史进度。

## 开发规范

- [环境与配置](./development/configuration.md)
- [质量验证](./development/verification.md)
- [数据库开发](./development/database.md)
- [新增业务聚合检查清单](./development/business-aggregate-checklist.md)

## 模块

- [采购申请](./modules/purchase-requisition.md)

每个可选业务模块都应记录边界、依赖、数据库迁移和移除方式。

## 计划与提案

- [项目路线图](./roadmap.md)
- [登录保护方案（提案）](./proposals/login-protection.md)

`roadmap.md` 只记录已批准但尚未完成的项目级事项。`proposals/` 中的内容尚未生效，不能作为实现依据。

## 历史归档

- [企业级架构优化计划（2026-07）](./archive/enterprise-architecture-optimization-plan-2026-07.md)
- [架构收口报告（2026-07）](./archive/architecture-closure-report-2026-07.md)

归档只用于追溯，不代表当前架构、规则或项目状态。
