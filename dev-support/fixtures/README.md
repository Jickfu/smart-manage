# 开发数据

本目录只保存开发者显式选择的数据，不属于 Flyway 自动迁移，也不由 dev/test profile 自动加载。

`multiple-departments.sql` 在已完成平台迁移的测试库中添加三个部门，根公司沿用平台 ID 1。使用 PostgreSQL 客户端的既有安全凭据配置，确认目标库后执行：

```bash
psql -d <明确的测试数据库> -v ON_ERROR_STOP=1 -f dev-support/fixtures/multiple-departments.sql
```

脚本不覆盖现有数据，重复导入会因主键或业务编码冲突失败。需要重建的库必须由使用者明确授权；选择演示领域不自动执行此文件，也不重置管理员密码。
