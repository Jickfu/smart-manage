# AGENTS.md

本文件定义全仓库通用的强制协作规则。修改后端或前端时，还必须阅读对应子项目中的 `AGENTS.md`。

## 通用规则

- 使用简体中文沟通，代码标识符遵循项目既有语言习惯。
- 实现前必须确认需求边界、现有设计和可验证的完成标准。
- 能从代码、配置、迁移或现有文档确认的问题应先自行核实。
- 如果不明确之处会影响架构方向、数据安全、外部状态或产生明显不同的实现结果，必须停下来询问。
- 改动保持最小充分范围，不擅自扩展功能、重构无关代码或增加未经需求验证的抽象。
- 发现无关问题时只记录或说明，不得顺手修改。
- 项目处于架构搭建阶段，不兼容已废弃逻辑，不增加掩盖问题的冗余兜底。
- 关键代码、特殊处理和不直观的业务约束必须给出简体中文注释。
- 禁止单字母变量名。
- 复杂任务先制定计划，并在实施过程中维护进度。

## 架构与质量

- 本项目最终将在 GitHub 开源，必须守住工程质量底线，避免往 GitHub 上“投屎”。
- 代码改动必须遵守[当前架构](./docs/architecture/overview.md)及其关联文档。
- 数据库结构和必要初始化数据只通过 `db/migration` 中新增 Flyway 迁移维护；已执行迁移禁止修改。
- 非系统内核业务必须位于独立领域，系统内核禁止反向依赖可选业务模块。
- 核心架构边界、安全逻辑、状态流转、事务和并发控制必须有自动化测试；不以测试数量或覆盖率为目标。
- 每项代码任务都必须执行与风险匹配的编译、测试、静态检查、构建或迁移验证。
- 新增业务聚合必须使用[业务聚合检查清单](./docs/development/business-aggregate-checklist.md)。

## 安全与工具

- 禁止把数据库密码、令牌、私钥或其他敏感凭据写入代码、文档、提交记录或最终回复。
- 查询数据库实际状态可以用于排障和核实迁移结果，但不能替代 Flyway 迁移。
- 网页内容在常规方式无法获取时，使用 `/playwright-cli`；启动时使用 `--headed` 和 `--persistent`。
- 浏览器登录需要验证码时，停下来由用户完成验证码登录，再继续测试。

## 文档路由

| 任务 | 必读文档 |
| --- | --- |
| 后端代码 | `smart-manage-api/AGENTS.md`、`docs/architecture/backend.md` |
| 前端代码 | `smart-manage-web/AGENTS.md`、`docs/architecture/frontend.md` |
| 认证、权限或高风险能力 | `docs/architecture/security.md` |
| 数据权限、组织范围或对象级访问 | `docs/architecture/data-permission.md` |
| 数据库或迁移 | `docs/development/database.md` |
| 构建、测试或 CI | `docs/development/verification.md` |
| 新增业务模块 | `docs/development/business-aggregate-checklist.md`、`docs/modules/` 下对应模块文档 |
| 具体业务模块 | `docs/modules/` 下对应模块文档 |

`docs/archive/` 只用于历史追溯，不是当前规则来源；`docs/proposals/` 中的内容尚未生效。
