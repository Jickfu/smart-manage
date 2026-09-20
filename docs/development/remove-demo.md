# 二开移除 Demo

上游保留 Demo 作为业务开发样板，并持续测试。二开项目通过删除源码、调整发行装配声明完成裁剪，CI 工作流不需要修改。

## 源码与装配配置

1. 删除 `smart-manage-server/domains/demo` 和 `smart-manage-web/src/domain/demo`。Demo 的迁移、专属数据库测试和模块文档随目录一起删除。
2. 在 `smart-manage-server/pom.xml` 的 `with-domains` profile 中删除 `domains/demo` 模块声明。
3. 在 `smart-manage-server/bootstrap/pom.xml` 的同名 profile 中删除 `smart-manage-domain-demo` 依赖，并从 `smartManage.expectedDomains` 中移除 `demo`。只有平台时取值为 `sys`；保留通用 profile，后续业务领域仍在此装配。
4. 从 `smart-manage-web/domains.json` 中移除 `demo`，至少保留 `sys`。更新本地与部署环境中的显式 `sys,demo` 选择，使用 `sys` 或 `all`。
5. 更新 README、文档索引、模块样板目录中的 Demo 命令和链接。独立测试夹具中的 Demo 名称可替换为自己的示例，但保留裁剪、缺失领域拒绝、分块归属等通用测试。

新增领域同理：声明父 POM 模块、bootstrap 依赖及期望领域、前端完整发行清单。CI 只调用平台与完整发行入口，不需要添加新的领域矩阵。前后端分别显式配置，由维护者决定发行组合；不会扫描目录后悄悄装配未声明代码，也不会因声明的文件缺失而跳过验证。

可用 `rg -n -i 'demo' .github scripts smart-manage-server smart-manage-web docs README.md` 核对剩余引用，逐项判断，不批量替换通用示例或第三方内容。新业务使用自己的领域、迁移链、功能键和权限码。

## 验证

在仓库根目录执行：

```powershell
pwsh -NoProfile -File scripts/verify-module-conventions.ps1
pwsh -NoProfile -File scripts/verify-baseline.tests.ps1
mvn -f smart-manage-server/pom.xml verify
mvn -f smart-manage-server/pom.xml -Pwith-domains verify
pwsh -NoProfile -File scripts/verify-baseline.ps1
pwsh -NoProfile -File scripts/verify-baseline.ps1 -WithDomains
```

在 `smart-manage-web` 下分别以 `SMART_MANAGE_DOMAINS=sys`、`all` 执行 `pnpm gen:registry`、`pnpm lint`、`pnpm format:check`、`pnpm test` 和 `pnpm build`。最后恢复 `sys` 并重新生成两个注册表。数据库连接条件见[质量验证](./verification.md)。

删除源码不等于卸载已有数据库：Demo 表、Flyway 历史、应用、菜单、权限及演示部门不会自动消失。已有库按[数据库发布与升级规则](./database.md#正式发布与升级规则)另行设计迁移，不通过删除历史表或修改已执行脚本假装完成卸载。
