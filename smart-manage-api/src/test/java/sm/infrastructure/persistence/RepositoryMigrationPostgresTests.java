package sm.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 在空库验证已创建的临时库中运行仓库实际业务迁移，不能连接开发或生产库。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class RepositoryMigrationPostgresTests {
    @Test
    void repositoryChainsMigrateAndRestartWithoutRewritingPlatformHistory() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                System.getProperty("smartManage.testDbUrl"), System.getProperty("smartManage.testDbUser"),
                System.getProperty("smartManage.testDbPassword"));
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        var platformHistory = jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank");
        Flyway platform = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                .cleanDisabled(true).validateMigrationNaming(true).validateOnMigrate(true)
                .ignoreMigrationPatterns(new String[0]).load();
        BusinessMigrationProperties properties = new BusinessMigrationProperties();
        String location = System.getProperty("smartManage.testBusinessLocation");
        if (location != null) {
            properties.setEnabled(true);
            properties.setLocation(location);
            properties.setMinimumPlatformVersion(System.getProperty("smartManage.testBusinessMinimumPlatformVersion", "2"));
        }
        DualFlywayMigrationStrategy strategy = new DualFlywayMigrationStrategy(properties);
        strategy.migrate(platform);
        var businessHistory = properties.isEnabled()
                ? jdbc.queryForList("SELECT * FROM flyway_business_schema_history ORDER BY installed_rank") : null;
        strategy.migrate(platform);
        assertEquals(platformHistory, jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank"));
        if (properties.isEnabled()) {
            assertEquals(businessHistory,
                    jdbc.queryForList("SELECT * FROM flyway_business_schema_history ORDER BY installed_rank"));
        }
    }
}
