package sm.domain.demo;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import sm.infrastructure.persistence.FlywayMigrationConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 演示数据断言随 Demo 一起裁剪；只在验证脚本创建的临时数据库中执行。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class DemoMigrationPostgresTests {
    @Test
    void demoMigratesAndRestartsWithOnlyItsExpectedSeedData() {
        String url = System.getProperty("smartManage.testDbUrl");
        String username = System.getProperty("smartManage.testDbUser");
        String password = System.getProperty("smartManage.testDbPassword");
        var verification = new JdbcTemplate(new DriverManagerDataSource(url, username, password));
        String currentDatabase = verification.queryForObject("SELECT current_database()", String.class);
        assertNotNull(currentDatabase);
        assertTrue(currentDatabase.startsWith("smart_manage_verify_"), "只允许从空库验证临时库创建测试库");
        String database = "smart_manage_demo_verify_" + UUID.randomUUID().toString().replace("-", "");
        String demoUrl = url.substring(0, url.lastIndexOf('/') + 1) + database;
        // 平台基线固定 public；必须独占数据库，避免其他领域的初始化数据影响专属断言。
        verification.execute("CREATE DATABASE " + database);
        try (var context = new AnnotationConfigApplicationContext(FlywayMigrationConfig.class)) {
            var source = new DriverManagerDataSource(demoUrl, username, password);
            var jdbc = new JdbcTemplate(source);
            var platform = Flyway.configure().dataSource(source)
                    .locations("classpath:db/platform/migration").cleanDisabled(true).validateMigrationNaming(true).load();
            var strategy = context.getBean(FlywayMigrationStrategy.class);
            strategy.migrate(platform);
            strategy.migrate(platform);
            assertEquals(3, jdbc.queryForObject("SELECT count(*) FROM flyway_demo_schema_history WHERE version IN ('1','2','3') AND success", Integer.class));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_demo_purchase_requisition", Integer.class));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_sys_feature WHERE feature_key = 'demo/procurement'", Integer.class));
            assertEquals(4, jdbc.queryForObject("SELECT count(*) FROM t_sys_org", Integer.class));
            assertEquals(3, jdbc.queryForObject("SELECT count(*) FROM t_sys_org WHERE parent_id = 1 AND org_type = 'DEPARTMENT' AND number IN ('101','102','103')", Integer.class));
            assertEquals(Boolean.TRUE, jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM t_sys_feature WHERE feature_key LIKE 'demo/%')", Boolean.class));
        } finally {
            verification.execute("DROP DATABASE " + database + " WITH (FORCE)");
        }
    }
}
