# 后端开发规则

本文件适用于 `smart-manage-api`，同时继承根目录 `AGENTS.md`。

后端架构、分层、接口、事务、命名、外部调用和依赖治理规则以[后端架构](../docs/architecture/backend.md)为唯一事实来源。涉及认证、权限或高风险能力时还必须阅读[安全架构](../docs/architecture/security.md)；涉及数据库或迁移时必须阅读[数据库开发](../docs/development/database.md)。本文件不重复这些规则的具体条款。

新增或显著扩展后端业务模块时，必须先阅读[模块开发指南](../docs/development/module-development-guide.md)和[模块样板目录](../docs/development/module-pattern-catalog.md)，确定模块类型、参考实现、公开入口、事务边界和风险验证。

## 验证

后端编译、测试、迁移和风险匹配验证统一遵守[质量验证](../docs/development/verification.md)，本文件不重复维护具体要求。
