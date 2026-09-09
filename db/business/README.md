# 业务扩展迁移

此目录由二次开发者维护，构建后位于 `classpath:db/business`，与平台 `db/migration` 分别扫描、分别记账。上游不在这里放置示例 SQL，避免示例在真实数据库自动执行。

首次建立业务链时从 `V1__create_business_tables.sql` 开始，版本必须大于 0；后续所有业务模块共用这条链，继续递增，不能每个模块重新使用 V1。业务链默认关闭；添加实际迁移后，通过 `SMART_MANAGE_BUSINESS_MIGRATION_ENABLED=true` 启用，并用 `SMART_MANAGE_BUSINESS_MINIMUM_PLATFORM_VERSION` 声明该业务发行包要求的平台数据库版本（默认 2）。

业务链使用同一数据源和默认 schema、独立的 `flyway_business_schema_history`，始终在平台迁移成功后运行。首次只在自己的历史表登记版本 0，再执行全部业务 V1+；不会把已有平台版本当成业务已执行版本，也不会改变平台历史。

若业务对象已经通过手工 SQL 或旧平台迁移建立，先制定一次性的接管方案，不得直接启用后自动猜测、跳过脚本或修补历史。启用后不得通过关闭业务链绕过失败。

验证命令：`pwsh -File db/verify-baseline.ps1`。脚本检测到本目录 SQL 后会同时验证业务链；非默认依赖版本通过 `-BusinessMinimumPlatformVersion` 指定。独立目录部署可使用 `-BusinessMigrationLocation`，应与应用的 `SMART_MANAGE_BUSINESS_MIGRATION_LOCATION` 一致。

完整的版本、对象所有权、升级和故障恢复规则见[数据库开发](../../docs/development/database.md)。
