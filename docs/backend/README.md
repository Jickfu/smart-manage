# 后端规范

后端开发的统一阅读入口，适用于开发人员与 Agent。具体约束继续由架构、开发和领域文档维护，本文只提供任务路由，不另建接口、事务或数据库规则副本。

## 按任务阅读

| 任务 | 权威正文 | 关注边界 |
| --- | --- | --- |
| 分层、依赖与领域装配 | [后端架构](../architecture/backend.md) | 组合根、平台与可选领域、跨领域 Contract |
| 接口、分页与异常 | [接口基础设施](../architecture/backend.md#接口基础设施) | Result、PageForm、PageData、访问级别；文件流单独处理 |
| 写操作与事务组合 | [Service 与事务](../architecture/backend.md#service-与事务) | 公开 Service、TxService、Writer 与事务参与 Contract |
| 日志、映射、查询模型与时间 | [后端架构](../architecture/backend.md) | 日志脱敏、Converter 纯映射、持久化模型与投影职责 |
| 外部调用、缓存、文件与任务 | [后端架构](../architecture/backend.md) | 超时、结果不确定、重试及资源生命周期 |
| 认证与功能权限 | [安全架构](../architecture/security.md)、[功能与权限](../architecture/feature-and-permission.md) | 公开、登录、权限接口及高风险能力 |
| 对象与隐私数据 | [数据权限](../architecture/data-permission.md)、[数据脱敏](../architecture/data-masking.md) | 模块显式接入，不假定公共能力已覆盖所有资源 |
| 表结构、SQL 与发布升级 | [数据库开发](../development/database.md) | 对象所有权、迁移冻结、向前升级与恢复 |
| 状态、不变量与业务模块 | [单据状态](../architecture/bill-status.md)、[模块开发指南](../development/module-development-guide.md) | 领域语义仍归各模块文档 |
| 配置、部署与多实例 | [环境与配置](../development/configuration.md)、[部署架构](../architecture/deployment.md) | 共享状态、迁移协调、备份恢复 |
| 验证与交付 | [质量验证](../development/verification.md) | 按风险选择检查；真实数据库验证不能由普通单测替代 |

## 协作与验证

协作要求按根目录与所属后端子项目 AGENTS 执行。新增或显著扩展模块使用[模块开发指南](../development/module-development-guide.md)及样板；验证命令和风险门槛统一查阅[质量验证](../development/verification.md)。具体业务语义由所属领域文档维护，不能以某个演示模块的字段或流程定义全部业务扩展。

平台实现读取 `smart-manage-server/platform/AGENTS.md`，可选领域读取 `smart-manage-server/domains/{领域}/AGENTS.md`；涉及组合根时再读取 `smart-manage-server/app/AGENTS.md`，并遵守这些入口显式引用的规则。当前领域与组合根 AGENTS 均引用平台后端规则；不假定存在后端根目录 AGENTS，也不从目录名称推断继承关系。扩展任务的阅读顺序见[代表任务路径](../development/collaboration.md#代表任务路径)。
