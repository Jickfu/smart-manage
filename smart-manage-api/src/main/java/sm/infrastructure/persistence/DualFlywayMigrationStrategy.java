package sm.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;

import java.nio.file.Path;
import java.util.Arrays;

/** 平台与业务分别记账、顺序迁移；业务失败不会伪装成平台事务已回滚。 */
final class DualFlywayMigrationStrategy implements FlywayMigrationStrategy {
    static final String BUSINESS_HISTORY_TABLE = "flyway_business_schema_history";
    private static final MigrationVersion INITIAL_VERSION = MigrationVersion.fromVersion("0");
    private final BusinessMigrationProperties properties;

    DualFlywayMigrationStrategy(BusinessMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void migrate(Flyway platform) {
        if (!properties.isEnabled()) {
            platform.migrate();
            return;
        }
        MigrationVersion minimumVersion = requireConfiguration(platform);
        platform.migrate();
        MigrationInfo current = platform.info().current();
        if (current == null || current.getVersion() == null
                || current.getVersion().compareTo(minimumVersion) < 0) {
            throw new IllegalStateException("业务迁移要求平台数据库至少达到版本 " + minimumVersion);
        }

        FluentConfiguration businessConfiguration = Flyway.configure()
                .dataSource(platform.getConfiguration().getDataSource())
                .locations(properties.getLocation())
                .table(BUSINESS_HISTORY_TABLE)
                .validateMigrationNaming(true)
                .validateOnMigrate(true)
                .outOfOrder(false)
                .cleanDisabled(true)
                .ignoreMigrationPatterns(new String[0])
                .failOnMissingLocations(true)
                // 平台迁移完成后 schema 必然非空。仅给新业务历史表登记 0，不跳过任何业务 V1+。
                .baselineOnMigrate(true)
                .baselineVersion(INITIAL_VERSION)
                .baselineDescription("business-chain-initialized");
        String defaultSchema = platform.getConfiguration().getDefaultSchema();
        if (defaultSchema != null) {
            businessConfiguration.defaultSchema(defaultSchema);
        }
        businessConfiguration.schemas(platform.getConfiguration().getSchemas());
        Flyway business = businessConfiguration.load();
        MigrationInfo[] migrations = business.info().all();
        boolean hasVersionedMigration = false;
        for (MigrationInfo migration : migrations) {
            if (migration.getVersion() == null || migration.getType().isSynthetic()) {
                continue;
            }
            if (migration.getVersion().compareTo(INITIAL_VERSION) <= 0) {
                throw new IllegalStateException("业务迁移版本必须大于 0；版本 0 专用于初始化业务历史表");
            }
            hasVersionedMigration = true;
        }
        if (!hasVersionedMigration) {
            throw new IllegalStateException("已启用业务迁移，但业务目录没有版本迁移文件");
        }
        business.migrate();
    }

    private MigrationVersion requireConfiguration(Flyway platform) {
        if (BUSINESS_HISTORY_TABLE.equals(platform.getConfiguration().getTable())) {
            throw new IllegalArgumentException("平台与业务必须使用不同的 Flyway 历史表");
        }
        String businessLocation = normalizeLocation(properties.getLocation());
        boolean overlaps = Arrays.stream(platform.getConfiguration().getLocations())
                .map(Location::getDescriptor)
                .map(DualFlywayMigrationStrategy::normalizeLocation)
                .anyMatch(location -> businessLocation.equals(location)
                        || businessLocation.startsWith(location + "/")
                        || location.startsWith(businessLocation + "/"));
        if (overlaps) {
            throw new IllegalArgumentException("平台与业务迁移目录不能相同或互相包含");
        }
        String requiredVersion = properties.getMinimumPlatformVersion();
        if (requiredVersion == null || !requiredVersion.matches("[0-9]+([._][0-9]+)*")) {
            throw new IllegalArgumentException("业务迁移必须声明有效的 minimum-platform-version");
        }
        MigrationVersion version = MigrationVersion.fromVersion(requiredVersion);
        if (version.compareTo(INITIAL_VERSION) <= 0) {
            throw new IllegalArgumentException("minimum-platform-version 必须大于 0");
        }
        return version;
    }

    private static String normalizeLocation(String location) {
        if (location == null || location.isBlank() || location.matches(".*[?*].*")) {
            throw new IllegalArgumentException("迁移目录必须明确指定，不能使用通配符");
        }
        if (location.startsWith("filesystem:")) {
            return "filesystem:" + Path.of(location.substring("filesystem:".length()))
                    .toAbsolutePath().normalize().toString().replace('\\', '/');
        }
        if (location.startsWith("classpath:")) {
            String path = location.substring("classpath:".length()).replace('\\', '/');
            if (path.isBlank() || path.startsWith("/")) {
                throw new IllegalArgumentException("classpath 迁移目录必须是明确的相对路径");
            }
            return "classpath:" + Path.of(path).normalize().toString().replace('\\', '/').replaceAll("/+$", "");
        }
        throw new IllegalArgumentException("迁移目录必须使用 classpath: 或 filesystem: 前缀");
    }
}
