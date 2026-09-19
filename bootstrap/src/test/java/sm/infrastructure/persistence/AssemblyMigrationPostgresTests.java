package sm.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.*;

/** 在实际发行 classpath 中验证声明发现、平台优先以及可选领域的目录和数据。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class AssemblyMigrationPostgresTests {
    @Test
    void selectedAssemblyMigratesAndRestartsWithoutChangingPlatformHistory() {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        var jdbc = new JdbcTemplate(source);
        var history = jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank");
        var platform = Flyway.configure().dataSource(source).locations("classpath:db/platform/migration")
                .cleanDisabled(true).validateMigrationNaming(true).load();
        try (var context = new AnnotationConfigApplicationContext(FlywayMigrationConfig.class)) {
            var strategy = context.getBean(FlywayMigrationStrategy.class);
            strategy.migrate(platform);
            strategy.migrate(platform);
        }
        assertEquals(history, jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank"));
        boolean withDemo = "demo".equals(System.getProperty("smartManage.expectedDomain"));
        assertEquals(withDemo, jdbc.queryForObject("SELECT to_regclass('t_demo_purchase_requisition') IS NOT NULL", Boolean.class));
        assertEquals(withDemo, jdbc.queryForObject("SELECT to_regclass('flyway_demo_schema_history') IS NOT NULL", Boolean.class));
        assertEquals(withDemo, jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM t_sys_feature WHERE feature_key LIKE 'demo/%')", Boolean.class));
        if (withDemo) {
            assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM flyway_demo_schema_history WHERE version IN ('1','2') AND success", Integer.class));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_demo_purchase_requisition", Integer.class));
        }
    }
}
