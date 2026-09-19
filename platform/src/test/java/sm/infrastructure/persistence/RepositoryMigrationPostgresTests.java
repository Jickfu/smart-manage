package sm.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 在空库验证已创建的临时库中运行仓库实际平台迁移，不能连接开发或生产库。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class RepositoryMigrationPostgresTests {
    @Test
    void repositoryChainsMigrateAndRestartWithoutRewritingPlatformHistory() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                System.getProperty("smartManage.testDbUrl"), System.getProperty("smartManage.testDbUser"),
                System.getProperty("smartManage.testDbPassword"));
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertEquals("./smfiles/", jdbc.queryForObject(
                "SELECT local_dir FROM t_sys_file_config WHERE storage_type = 'LOCAL'", String.class));
        var platformHistory = jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank");
        Flyway platform = Flyway.configure().dataSource(dataSource).locations("classpath:db/platform/migration")
                .cleanDisabled(true).validateMigrationNaming(true).validateOnMigrate(true)
                .ignoreMigrationPatterns(new String[0]).load();
        platform.migrate();
        platform.migrate();
        assertEquals(platformHistory, jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank"));
    }
}
