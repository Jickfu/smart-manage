---
name: smart-manage-module
description: "Plan, implement, or significantly extend a Smart Manage business module using the repository's architecture, reference modules, page conventions, migrations, and risk-matched verification. Use for end-to-end modules, business aggregates, master data, configuration, records, or consoles; do not use for small local fixes that do not change module boundaries."
---

# Smart Manage 模块开发

本 skill 只编排按需开发流程。生效文档规定约束，代码和测试核实实现现状；不通过改文档将实现缺陷合法化。小范围局部修复不启动完整模块流程。

## 执行流程

1. **识别范围。** 检查工作区已有修改，明确模块类型、归属、完成标准和本次变更维度；保留用户已有工作。影响架构、数据安全或外部状态的未决边界先询问，其余先从仓库核实。
2. **读取相关规则。** 读取根目录和涉及子项目的 `AGENTS.md`，按路由补读相关架构、领域与风险资料。已加载且未变化的规则不重复读取。[模块开发指南](../../../docs/development/module-development-guide.md)按分类、关键决策和涉及层次读取；新聚合补读[聚合检查清单](../../../docs/development/business-aggregate-checklist.md)。
3. **选择样板并明确差异。** 从[样板目录](../../../docs/development/module-pattern-catalog.md)选择实际变更维度的参考实现和测试。纯后端扩展不检查无关前端细节，前端调整不强制阅读无关迁移与事务；跨层契约再补读关联部分。记录目标业务与样板在身份、状态、授权及交互上的差异，禁止复制样板业务身份或偶然字段。
4. **执行相关步骤。** 按模块指南完成范围内接入，跨层变更保持 Feature、权限、菜单、接口和页面的显式关联。页面布局、明细表格与操作反馈按[前端页面指南](../../../docs/development/frontend-page-guide.md)相应章节执行。新增模块或改变长期业务语义时补充最小必要领域文档，不因局部实现缺少文档生成整套模板。
5. **按风险验证。** 明确执行仓库 `scripts/verify-module-conventions.ps1`；命令、参数及其他必需检查统一按[质量验证](../../../docs/development/verification.md)。注册源变更后检查并保留首次预期生成结果，再生成验证稳定性；CI 干净检出检查已提交生成结果同步。数据库权限、凭据、事务或并发风险按该文档触发真实 PostgreSQL 验证，环境缺失如实标记未验证，不降低门槛。
6. **汇报证据。** 说明实际实现边界、必要设计差异、执行检查及结果、未验证项和待决问题。模块约定脚本通过不能替代业务、安全、并发或浏览器验证。
