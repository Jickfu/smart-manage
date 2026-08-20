# 参与开发

Smart Manage 仍处于架构搭建阶段。贡献应优先提高架构内核和工程质量，不批量铺设未经纵向验证的业务模块。

## 开始之前

1. 阅读[当前架构](./docs/architecture/overview.md)。
2. 根据改动范围阅读后端、前端、安全、数据库或模块文档。
3. 明确需求边界、完成标准和验证方式。
4. 检查工作区已有改动，避免覆盖无关工作。

使用 AI 编码代理时，还必须遵循根目录和对应子项目中的 `AGENTS.md`。

## 修改原则

- 每次修改只处理一个清晰主题。
- 保持最小充分范围，不夹带无关重构。
- 不为尚未出现的需求提前增加抽象、兼容层或功能开关。
- 数据库结构调整必须新增 Flyway 迁移。
- 关键架构、安全、事务、状态和并发逻辑必须补充自动化测试。
- 自动生成文件通过生成命令维护，不得手工修补生成结果。

## 提交前验证

后端代码至少执行：

```bash
cd smart-manage-api
mvn test
```

前端代码至少执行：

```bash
cd smart-manage-web
pnpm lint
pnpm format:check
pnpm test
pnpm build
```

数据库迁移、组件注册表及按改动类型选择验证的方法见[质量验证](./docs/development/verification.md)。

仅修改文档时，不要求运行代码构建，但必须检查文档链接、标题结构和事实一致性。

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
