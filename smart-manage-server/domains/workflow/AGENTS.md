# 工作流领域开发规则

本目录继承根目录 `AGENTS.md` 和[平台后端规则](../../platform/AGENTS.md)。

生效规则见[工作流领域](./docs/process/workflow.md)，背景见[工作流方案](../../../../docs/proposals/workflow-domain-design.md)，验收进度见[实施进度](../../../../docs/proposals/workflow-implementation-progress.md)。Warm-Flow 类型只留在 engine/warmflow，平台不得反向依赖本领域。撤回表示结束本轮，仅允许申请人在尚无实际审批时执行，不得直接映射引擎 revoke。

验证遵守[质量验证](../../../../docs/development/verification.md)，涉及审批、撤回或并发必须覆盖真实 PostgreSQL 事务与竞争，Mock 不替代数据库验收。
