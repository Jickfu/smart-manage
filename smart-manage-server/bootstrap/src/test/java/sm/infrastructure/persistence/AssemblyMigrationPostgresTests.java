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
    void selectedAssemblyMigratesAndRestartsWithoutChangingPlatformHistory() throws Exception {
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
        var expectedDomains = new java.util.HashSet<>(java.util.Set.of(
                System.getProperty("smartManage.expectedDomains", "sys").split(",")));
        expectedDomains.remove("sys");
        var actualDomains = new java.util.HashSet<String>();
        for (var resource : new org.springframework.core.io.support.PathMatchingResourcePatternResolver()
                .getResources("classpath*:META-INF/smart-manage/migration.properties")) {
            var declaration = new java.util.Properties();
            try (var input = resource.getInputStream()) { declaration.load(input); }
            assertTrue(actualDomains.add(declaration.getProperty("id")), "领域迁移声明不得重复");
            assertNotNull(jdbc.queryForObject("SELECT to_regclass(?)", String.class, declaration.getProperty("table")),
                    "已装配领域必须实际建立迁移历史表");
        }
        assertEquals(expectedDomains, actualDomains, "完整装配必须包含所有所选领域的迁移声明");
        if (expectedDomains.isEmpty()) {
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM t_sys_org", Integer.class));
        }
    }
}
