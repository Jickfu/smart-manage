# 质量验证

验证应与改动风险匹配。以下命令是代码修改的最低门槛，不能通过降低规则、忽略错误或手工修补生成文件来绕过。

## 模块约定

新增或显著扩展业务模块时，先执行仓库级确定性约束检查：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-module-conventions.ps1
```

该脚本检查文档与 skill 路由、页面注册字段、前端内联样式和 Controller 权限注解常量等文件或前端源码约束。Java 类型、包、注解和依赖边界统一由后端 ArchUnit 测试验证，脚本不重复扫描 Java import。业务状态、数据安全和交互语义仍必须通过评审及风险驱动测试验证。

当前检查清单如下：

| 分类 | 检查内容 | 违规后的修复方向 |
| --- | --- | --- |
| 治理文件 | 模块开发指南、模块模式目录、模块开发 skill 及其代理配置必须存在 | 恢复规定文件；规范调整时同步更新仓库治理入口 |
| 文档与 skill 路由 | 根目录及前后端 `AGENTS.md` 必须指向模块开发指南，模块开发 skill 必须调用本脚本 | 补充明确的文档路由或校验步骤，不能仅依赖代理记忆 |
| 前端样式 | `smart-manage-web/src/domain` 下的 TSX 文件不得使用内联 `style` | 使用项目既有样式文件、组件能力或设计 token |
| 操作反馈 | 除统一封装自身外，前端不得直接调用 Ant Design `message` 的操作反馈方法 | 改用 `useOperationFeedback` |
| 操作确认 | 前端不得直接调用 `Modal.confirm` 或使用 `Popconfirm` | 改用 `useOperationConfirm` |
| 后端权限 | Controller 的 `@SaCheckPermission` 不得直接填写权限编码字符串 | 引用对应模块的权限常量 |
| 页面注册 | 至少存在一个 `pageRegistration.ts` 或 `pageRegistration.tsx`，每个注册项都必须声明 `componentKey`、`featureKey` 和 `pageType`，且 `featureKey` 不得为空 | 补全显式注册字段，使页面与稳定功能身份建立关联 |

脚本使用源码静态扫描完成这些适合机械判断的确定性检查，不等价于完整的架构或业务验证。以下内容不由该脚本负责：

- Java 类型、包、注解、可见性和依赖边界，由后端 ArchUnit 测试验证。
- Feature、权限、菜单、迁移和页面注册之间基于真实数据的完整一致性，由对应测试和 CI 校验验证。
- 业务状态流转、事务、并发控制、数据安全及交互语义，由代码评审和风险驱动测试验证。
- 编译、单元测试、静态检查、前端构建及 Flyway 空库迁移，仍需执行本文件后续章节列出的命令。

新增、删除或调整脚本检查项时，必须同步更新脚本顶部帮助、本清单及相关成功或失败提示。可使用以下命令直接查看脚本内置说明：

```powershell
pwsh.exe -NoProfile -Command "Get-Help .\scripts\verify-module-conventions.ps1 -Detailed"
```

## 后端

修改后端代码至少执行：

```bash
cd smart-manage-api
mvn test
```

只有纯文档修改或确认不影响测试代码的简单改动才可以仅执行 `mvn compile`。实体、Mapper、配置和迁移变更还必须确认相关代码能够正常编译。

## 前端

修改前端代码至少执行：

```bash
cd smart-manage-web
pnpm lint
pnpm format:check
pnpm test
pnpm build
```

涉及页面注册时执行：

```bash
pnpm gen:registry
git diff --exit-code -- src/domain/common/registry/registry.gen.ts
```

生成文件只能通过生成命令维护。

## Flyway 空库验证

Windows 环境可以运行：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\db\verify-baseline.ps1
```

脚本默认从 PATH 查找 PostgreSQL Client 16，并在迁移前输出和校验 `psql` 版本；特殊本地安装可通过 `-PsqlPath` 显式指定可执行文件。脚本创建临时数据库，通过项目锁定版本的 Flyway 执行全部迁移，校验版本、命名、checksum 和 `flyway_schema_history`，并在验证后清理。数据库结构、初始化数据、迁移顺序或脚本发生变化时必须执行此项验证。

## CI 门禁

`.github/workflows/quality-gate.yml` 当前执行：

1. 模块约定脚本；
2. 后端 `mvn test`；
3. 前端依赖锁定安装；
4. `pnpm lint`；
5. `pnpm format:check`；
6. `pnpm test`；
7. `pnpm build`；
8. 组件注册表无差异检查；
9. PostgreSQL 16 上的 Flyway 空库迁移；
10. 使用迁移完成后的真实权限目录执行代码权限一致性校验。
11. 使用迁移完成后的真实功能目录校验全部页面注册的 `featureKey`，并校验菜单与入口权限属于同一功能。

主分支保护属于 GitHub 仓库外部设置，需要由仓库管理员启用并要求质量门禁通过。

## 按风险增加验证

- 架构边界：架构测试或静态检查。
- Java 类型、包、注解、可见性和依赖边界：优先扩展 `ArchitectureContractTests`，不得新增 regex/import 源码扫描与其重复校验。
- 认证、权限和安全：优先用行为测试验证拒绝路径和副作用顺序，用 ArchUnit 验证注解、类型和依赖边界；源码扫描只保留无法结构化表达的少量机械约束。
- 状态、事务和乐观锁：覆盖成功、非法状态、过期版本和回滚。
- 文件存储：覆盖上传、授权下载、删除失败后的补偿。
- 前端生命周期：覆盖缓存失效、临时页签替换、只读状态和脏数据关闭。
- 生产部署：验证 CSP、反向代理、敏感配置和被关闭的高风险入口。

不设置机械覆盖率或测试数量目标。
