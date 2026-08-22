---
name: smart-manage-module
description: "Plan, implement, or significantly extend a Smart Manage business module using the repository's architecture, reference modules, page conventions, migrations, and risk-matched verification. Use for end-to-end modules, business aggregates, master data, configuration, records, or consoles; do not use for small local fixes that do not change module boundaries."
---

# Smart Manage 模块开发

创建符合本仓库设计的模块，而不只是使用相同的技术栈。仓库文档和当前代码是事实来源；本 skill 只负责编排开发流程。

## 必读资料

实现前必须完整阅读：

1. 根目录 `AGENTS.md`，以及每个将要修改的子项目中的 `AGENTS.md`。
2. `docs/development/module-development-guide.md`。
3. `docs/development/module-pattern-catalog.md`。
4. 根目录 `AGENTS.md` 根据实际任务路由的架构、安全、数据库、验证和领域文档。
5. 如果属于业务聚合，还要阅读 `docs/development/business-aggregate-checklist.md`。

不要加载无关领域文档。如果必需的领域文档不存在，应在当前任务中确定最小充分的模块设计，并将对应领域文档作为实现的一部分补充。

## 工作流程

### 1. 建立实现基线

先检查当前代码、配置、迁移、测试和文档，再提出问题。明确并记录：

- 需求范围和可度量的完成标准；
- 模块类型及其 `{领域}/{应用}/{模块}` 归属；
- 稳定 `featureKey`、页面、权限、菜单入口及其他稳定身份；
- 状态命令、事务边界、数据归属、实体引用、敏感字段和外部副作用；
- 主参考模块及必要的专项辅助参考；
- 必须执行的验证。

只有未明确事项会改变架构方向、数据安全、外部状态或产生明显不同的产品结果时，才询问用户。没有真实使用方时，不得虚构未来功能或提前建立抽象。

### 2. 编码前比较参考实现

完整检查选定样板的领域文档、迁移、后端公开 Service 与事务 Service、前端类型/API/Query Key/权限/页面注册/页面，以及风险测试。记录目标模块与样板之间有意保留的差异。禁止直接复制样板的 featureKey、权限码、业务字段、CSS 类名或状态。

### 3. 实现最小完整纵向闭环

保持 Feature、权限、菜单、迁移、后端接口、前端注册、页面和测试之间的显式关联。保留用户已有改动，避免无关重构。

前端操作前确认统一使用 `useOperationConfirm` 并声明风险类型，操作结果统一使用 `useOperationFeedback`；模块页面不得重新引入 Ant Design 的 `Modal.confirm`、`Popconfirm` 或 `message.*`。

标准主数据和配置默认使用 `LIST + EDIT`。业务聚合必须执行聚合检查清单。只有通用页面模型无法表达真实业务交互时才使用 `CUSTOM`。

### 4. 执行确定性检查和风险匹配验证

运行：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-module-conventions.ps1
```

随后根据修改文件和风险，执行 `docs/development/verification.md` 要求的全部命令。修改页面注册时必须重新生成注册表并确认生成文件无差异；修改迁移时必须执行 Flyway 空库验证。

不得通过降低 lint、测试、生成器、迁移或架构检查标准使实现通过。

### 5. 使用证据交付

汇报已实现的边界、重要设计决策、修改的文件或区域、执行的命令及结果，以及尚未验证或明确延期的内容。模块约定脚本通过不能替代业务、安全、并发或浏览器验证。
