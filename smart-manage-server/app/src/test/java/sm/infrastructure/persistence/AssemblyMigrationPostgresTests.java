package sm.infrastructure.persistence;

import com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceAutoConfigure;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import sm.infrastructure.scheduler.QuartzConfiguration;

import javax.sql.DataSource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** 在实际发行 classpath 中验证声明发现、平台优先以及可选领域的目录和数据。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class AssemblyMigrationPostgresTests {
    @Test
    void applicationDataSourcesUseBeijingSessions() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class))
                .withUserConfiguration(DruidDataSourceConfig.class, QuartzConfiguration.class)
                .withPropertyValues(
                        "spring.datasource.url=" + System.getProperty("smartManage.testDbUrl"),
                        "spring.datasource.username=" + System.getProperty("smartManage.testDbUser"),
                        "spring.datasource.password=" + System.getProperty("smartManage.testDbPassword"))
                .run(context -> {
                    for (String beanName : java.util.List.of("dataSource", "quartzDataSource", "flywayDataSource")) {
                        var jdbc = new JdbcTemplate(context.getBean(beanName, DataSource.class));
                        assertEquals("Asia/Shanghai", jdbc.queryForObject("SHOW TimeZone", String.class), beanName);
                    }
                });
    }

    @Test
    void selectedAssemblyMigratesAndRestartsWithoutChangingPlatformHistory() throws Exception {
        var source = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        var jdbc = new JdbcTemplate(source);
        assertEquals("Asia/Shanghai", jdbc.queryForObject("SHOW TimeZone", String.class));
        assertEquals(0, jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns
                WHERE table_schema='public' AND table_name LIKE 't_sys_monitor_%'
                  AND data_type='timestamp with time zone'
                """, Integer.class));
        var ruleTime = LocalDateTime.parse("2026-08-25T14:45:23.512402");
        assertEquals(ruleTime, jdbc.queryForObject("""
                SELECT create_time FROM t_sys_monitor_alert_rule WHERE rule_code='HOST_CPU_HIGH'
                """, LocalDateTime.class));
        assertEquals(ruleTime, jdbc.queryForObject("""
                SELECT update_time FROM t_sys_monitor_alert_rule WHERE rule_code='HOST_CPU_HIGH'
                """, LocalDateTime.class));
        var history = jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank");
        var platform = Flyway.configure().dataSource(source).locations("classpath:db/platform/migration")
                .cleanDisabled(true).validateMigrationNaming(true).load();
        try (var context = new AnnotationConfigApplicationContext(FlywayMigrationConfig.class)) {
            var strategy = context.getBean(FlywayMigrationStrategy.class);
            strategy.migrate(platform);
            strategy.migrate(platform);
        }
        assertEquals(history, jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank"));
        var actualDomains = new java.util.HashSet<String>();
        for (var resource : new org.springframework.core.io.support.PathMatchingResourcePatternResolver()
                .getResources("classpath*:META-INF/smart-manage/migration.properties")) {
            var declaration = new java.util.Properties();
            try (var input = resource.getInputStream()) { declaration.load(input); }
            assertTrue(actualDomains.add(declaration.getProperty("id")), "领域迁移声明不得重复");
            assertNotNull(jdbc.queryForObject("SELECT to_regclass(?)", String.class, declaration.getProperty("table")),
                    "已装配领域必须实际建立迁移历史表");
        }
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_sys_feature_data_object "
                + "WHERE to_regclass(format('%I.%I', schema_name, object_name)) IS NULL", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_sys_feature_data_object data_object "
                + "JOIN pg_namespace namespace ON namespace.nspname = data_object.schema_name "
                + "JOIN pg_class catalog_object ON catalog_object.relnamespace = namespace.oid "
                + "AND catalog_object.relname = data_object.object_name "
                + "WHERE data_object.object_type IS DISTINCT FROM CASE catalog_object.relkind "
                + "WHEN 'r' THEN 'TABLE' WHEN 'p' THEN 'PARTITIONED_TABLE' WHEN 'v' THEN 'VIEW' "
                + "WHEN 'm' THEN 'MATERIALIZED_VIEW' WHEN 'f' THEN 'FOREIGN_TABLE' END", Integer.class));
        if (actualDomains.isEmpty()) {
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM t_sys_org", Integer.class));
        }
    }
}
