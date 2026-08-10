# 质量验证

验证应与改动风险匹配。以下命令是代码修改的最低门槛，不能通过降低规则、忽略错误或手工修补生成文件来绕过。

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

脚本创建临时数据库，通过项目锁定版本的 Flyway 执行全部迁移，校验版本、命名、checksum 和 `flyway_schema_history`，并在验证后清理。数据库结构、初始化数据、迁移顺序或脚本发生变化时必须执行此项验证。

## CI 门禁

`.github/workflows/quality-gate.yml` 当前执行：

1. 后端 `mvn test`；
2. 前端依赖锁定安装；
3. `pnpm lint`；
4. `pnpm format:check`；
5. `pnpm test`；
6. `pnpm build`；
7. 组件注册表无差异检查；
8. PostgreSQL 16 上的 Flyway 空库迁移；
9. 使用迁移完成后的真实权限目录执行代码权限一致性校验。

主分支保护属于 GitHub 仓库外部设置，需要由仓库管理员启用并要求质量门禁通过。

## 按风险增加验证

- 架构边界：架构测试或静态检查。
- 认证、权限和安全：单元/集成测试及必要浏览器验证。
- 状态、事务和乐观锁：覆盖成功、非法状态、过期版本和回滚。
- 文件存储：覆盖上传、授权下载、删除失败后的补偿。
- 前端生命周期：覆盖缓存失效、临时页签替换、只读状态和脏数据关闭。
- 生产部署：验证 CSP、反向代理、敏感配置和被关闭的高风险入口。

不设置机械覆盖率或测试数量目标。
