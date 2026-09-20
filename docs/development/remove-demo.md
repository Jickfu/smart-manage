# 二开移除 Demo

上游保留 Demo 作为业务开发样板，并持续测试。二开项目通过删除源码、调整发行装配声明完成裁剪，CI 工作流不需要修改。

## 源码与装配配置

1. 删除 `smart-manage-server/domains/demo` 和 `smart-manage-web/src/domain/demo`。Demo 的迁移、专属数据库测试和模块文档随目录一起删除。
2. 在 `smart-manage-server/domains/pom.xml` 的 `current-domains` profile 中删除 `demo` module 和 `smart-manage-domain-demo` dependency；保留聚合模块和 `platform-only` profile。
3. 更新本地与部署环境中显式写死的 `SMART_MANAGE_DOMAINS=sys,demo`；未设置或设为 `all` 时，前端会自动发现删除后剩余的领域。
4. 更新 README、文档索引、模块样板目录中的 Demo 命令和链接。独立测试夹具中的 Demo 名称可替换为自己的示例，但保留裁剪、缺失领域拒绝、分块归属等通用测试。

新增领域同理：在 `domains/pom.xml` 声明 module 与 dependency，并创建同名前端领域目录。CI 只调用默认当前装配与平台隔离入口，不需要添加新的领域矩阵；一致性门禁会拒绝只改一端、后端声明缺失目录或迁移标识不一致。

可用 `rg -n -i 'demo' .github scripts smart-manage-server smart-manage-web docs README.md` 核对剩余引用，逐项判断，不批量替换通用示例或第三方内容。新业务使用自己的领域、迁移链、功能键和权限码。

## 验证

在仓库根目录执行：

```powershell
pwsh -NoProfile -File scripts/verify-module-conventions.ps1
pwsh -NoProfile -File scripts/verify-baseline.tests.ps1
mvn -f smart-manage-server/pom.xml verify
mvn -f smart-manage-server/pom.xml -Pplatform-only verify
pwsh -NoProfile -File scripts/verify-baseline.ps1
pwsh -NoProfile -File scripts/verify-baseline.ps1 -PlatformOnly
```

在 `smart-manage-web` 下分别以未设置 `SMART_MANAGE_DOMAINS`、显式 `SMART_MANAGE_DOMAINS=sys` 执行 `pnpm gen:registry`、`pnpm lint`、`pnpm format:check`、`pnpm test` 和 `pnpm build`。最后清除变量并重新生成两个注册表。数据库连接条件见[质量验证](./verification.md)。

删除源码不等于卸载已有数据库：Demo 表、Flyway 历史、应用、菜单、权限及演示部门不会自动消失。已有库按[数据库发布与升级规则](./database.md#正式发布与升级规则)另行设计迁移，不通过删除历史表或修改已执行脚本假装完成卸载。
