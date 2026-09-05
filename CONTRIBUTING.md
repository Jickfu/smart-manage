# 参与开发

Smart Manage 仍处于架构搭建阶段。贡献应优先提高架构内核和工程质量，不批量铺设未经纵向验证的业务模块。

## 开始之前

1. 阅读[当前架构](./docs/architecture/overview.md)。
2. 根据改动范围阅读后端、前端、安全、数据库或模块文档。
3. 明确需求边界、完成标准和验证方式。
4. 检查工作区已有改动，避免覆盖无关工作。

使用 AI 编码代理时，还必须遵循根目录和对应子项目中的 `AGENTS.md`。

## 开发与验证

保持单一主题和最小充分范围，不夹带无关重构。按[文档导航](./docs/README.md#按任务阅读)读取涉及的架构和领域规则；新增或显著扩展模块执行[模块开发指南](./docs/development/module-development-guide.md)，普通页面调整按[前端页面指南](./docs/development/frontend-page-guide.md)。

提交前按[质量验证](./docs/development/verification.md#按改动类型选择验证)执行对应检查，并在提交说明中如实列出结果和未验证项。纯文档修改无需代码构建；快速启动命令见 [README](./README.md#快速启动)。

## 提交规范

提交信息和 Pull Request 标题遵循 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/)：

```text
<type>(<scope>): <description>
```

常用 `type` 包括 `feat`、`fix`、`refactor`、`docs`、`test`、`style`、`build`、`ci` 和 `chore`。`scope` 使用稳定、简短的模块或领域名称；`description` 使用简体中文准确描述改动，不以句号结尾。

不兼容变更使用 `!` 标记，并在正文中说明影响和迁移方式：

```text
feat(api)!: 调整统一响应结构
```

## 提交说明

提交或 Pull Request 应说明：

- 解决的问题和需求边界；
- 采用的设计及关键取舍；
- 实际修改范围；
- 已执行的验证及结果；
- 已知但不属于本次范围的问题。
