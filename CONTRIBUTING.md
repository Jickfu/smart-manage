# 参与开发

Smart Manage 是可二次开发的企业中后台底座。贡献范围与阶段门槛遵守[项目路线图](./docs/roadmap.md)，已发布版本的数据库变更遵守[数据库开发](./docs/development/database.md#正式发布与升级规则)。

## 开始之前

1. 阅读[当前架构](./docs/architecture/overview.md)。
2. 根据改动范围阅读后端、前端、安全、数据库或模块文档。
3. 明确需求边界、完成标准和验证方式。
4. 检查工作区已有改动，避免覆盖无关工作。

使用 AI 编码代理时，还必须遵循根目录和对应子项目中的 `AGENTS.md`。

## 分支协作

仓库采用以 `main` 为唯一长期分支的主干开发模式，不设置长期 `develop` 分支。`main` 应始终满足质量门禁，并作为正式发布标签的来源。

普通改动从最新 `main` 创建单一主题的短期分支，推荐使用 `feat/`、`fix/`、`refactor/`、`docs/` 等前缀；Codex 创建的分支使用 `codex/` 前缀。改动通过 Pull Request 合入 `main`，合并前必须与最新主分支同步、解决评审对话并通过全部必需检查。仓库只使用 squash merge，合并后删除源分支。

只有在正式发布后确实需要并行维护旧版本时，才从对应版本基线建立 `release/<版本>` 维护分支；不为尚未发生的多版本维护预建 `release` 或 `hotfix` 分支。

## 开发与验证

保持单一主题和最小充分范围，不夹带无关重构。按[文档导航](./docs/README.md#按任务阅读)读取涉及的架构和领域规则；新增或显著扩展模块执行[模块开发指南](./docs/development/module-development-guide.md)，普通页面调整按[前端页面指南](./docs/development/frontend-page-guide.md)。

提交前按[质量验证](./docs/development/verification.md#按改动类型选择验证)执行对应检查，并在提交说明中如实列出结果和未验证项。纯文档修改无需代码构建；快速启动命令见 [README](./README.md#快速开始)。

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
