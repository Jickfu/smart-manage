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
- 当前生效的架构、开发和验证规则以本文件“文档路由”指向的 `docs/` 文档为唯一事实来源；`AGENTS.md` 不复制其具体条款。
- 实现前必须按任务路由阅读对应权威文档，并按[质量验证](./docs/development/verification.md)执行与风险匹配的验证。
- 新增或显著扩展业务模块必须先使用[模块开发指南](./docs/development/module-development-guide.md)完成模块分类、样板选择、边界设计和验收基线；新增业务聚合还必须使用[业务聚合检查清单](./docs/development/business-aggregate-checklist.md)。
- 使用支持仓库 skills 的开发代理时，新增或显著扩展业务模块应调用 `$smart-manage-module`；skill 只编排流程，仓库文档和当前代码仍是事实来源。
- 适合机械判断的项目约束必须通过测试或 `scripts/verify-module-conventions.ps1` 校验，不得只依赖人工记忆或提示词。

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
| 数据脱敏、隐私字段或凭据清除 | `docs/architecture/data-masking.md`、`docs/architecture/security.md` |
| 功能目录、菜单、权限或页面注册 | `docs/architecture/feature-and-permission.md`、`docs/architecture/security.md` |
| 数据权限、组织范围或对象级访问 | `docs/architecture/data-permission.md` |
| 数据库或迁移 | `docs/development/database.md` |
| 构建、测试或 CI | `docs/development/verification.md` |
| 新增或显著扩展业务模块 | `docs/development/module-development-guide.md`、`docs/development/module-pattern-catalog.md`、`docs/domains/{领域}/{应用}/` 下对应模块文档 |
| 新增业务聚合 | 上述模块文档及 `docs/development/business-aggregate-checklist.md` |
| 具体业务模块 | `docs/domains/{领域}/{应用}/` 下对应模块文档 |

`docs/archive/` 只用于历史追溯，不是当前规则来源；`docs/proposals/` 中的内容尚未生效。
