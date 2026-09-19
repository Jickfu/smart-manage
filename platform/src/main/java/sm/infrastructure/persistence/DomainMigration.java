package sm.infrastructure.persistence;

/** 构建装配的领域迁移元数据，不参与业务领域之间的依赖。 */
record DomainMigration(String id, String location, String table, String minimumPlatformVersion) {
    DomainMigration {
        if (id == null || !id.matches("[a-z][a-z0-9_]*")
                || table == null || !table.matches("[a-z][a-z0-9_]*")) {
            throw new IllegalArgumentException("领域迁移标识和历史表必须是安全的固定名称");
        }
    }
}
