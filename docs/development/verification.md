# 质量验证

验证应与改动风险匹配。以下命令是代码修改的最低门槛，不能通过降低规则、忽略错误或手工修补生成文件来绕过。

## 模块约定

新增或显著扩展业务模块时，先执行仓库级确定性约束检查：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-module-conventions.ps1
```

该脚本检查文档与 skill 路由、页面注册字段、前端内联样式和 Controller 权限注解常量等文件或前端源码约束。Java 类型、包、注解和依赖边界统一由后端 ArchUnit 测试验证，脚本不重复扫描 Java import。业务状态、数据安全和交互语义仍必须通过评审及风险驱动测试验证。

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
