package sm.infrastructure.persistence;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;
/** 从本次变更前的真实V3开发基线升级，验证增量不覆盖用户已有配置。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class SingletonMigrationPostgresTests {
    @Test void upgradesV3WithDataAndMigratesAgainWithoutChangingHistory() {
        String url = System.getProperty("smartManage.testDbUrl");
        String username = System.getProperty("smartManage.testDbUser");
        String password = System.getProperty("smartManage.testDbPassword");
        var verification = new JdbcTemplate(new DriverManagerDataSource(url, username, password));
        assertTrue(verification.queryForObject("SELECT current_database()", String.class).startsWith("smart_manage_verify_"));
        String database = "smart_manage_singleton_upgrade_" + UUID.randomUUID().toString().replace("-", "");
        boolean created = false;
        try {
            verification.execute("CREATE DATABASE " + database);
            created = true;
            var source = new DriverManagerDataSource(url.substring(0, url.lastIndexOf('/') + 1) + database, username, password);
            Flyway.configure().dataSource(source).locations("classpath:db/platform/migration").target("3").cleanDisabled(true).load().migrate();
            var jdbc = new JdbcTemplate(source);
            jdbc.update("UPDATE t_sys_password_config SET min_length=22,complexity_enabled=true,version=version+1 WHERE id=1");
            var before = jdbc.queryForMap("SELECT * FROM t_sys_password_config WHERE id=1");
            var jobs = jdbc.queryForList("SELECT * FROM t_sys_job ORDER BY id");
            var flyway = Flyway.configure().dataSource(source).locations("classpath:db/platform/migration").cleanDisabled(true).load();
            assertEquals(1, flyway.migrate().migrationsExecuted);
            assertEquals(before, jdbc.queryForMap("SELECT * FROM t_sys_password_config WHERE id=1"));
            assertEquals(jobs, jdbc.queryForList("SELECT * FROM t_sys_job ORDER BY id"));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_sys_singleton_task", Integer.class));
            assertEquals(3, jdbc.queryForObject("SELECT count(*) FROM t_sys_permission WHERE feature_id=560000000000000300", Integer.class));
            var history = jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank");
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertEquals(history, jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank"));
        } finally {
            if (created) verification.execute("DROP DATABASE " + database);
        }
    }
}
