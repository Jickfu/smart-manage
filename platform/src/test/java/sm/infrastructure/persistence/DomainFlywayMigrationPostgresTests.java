package sm.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 每个用例独占测试 schema，使用真实 Flyway 锁、校验和事务验证两条版本链。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class DomainFlywayMigrationPostgresTests {
    @TempDir Path directory;
    private Path platformDirectory;
    private Path businessDirectory;
    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbc;
    private String schema;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new DriverManagerDataSource(System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"), System.getProperty("smartManage.testDbPassword"));
        jdbc = new JdbcTemplate(dataSource);
        schema = "dual_migration_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE SCHEMA " + schema);
        platformDirectory = Files.createDirectory(directory.resolve("platform"));
        businessDirectory = Files.createDirectory(directory.resolve("business"));
        Files.writeString(platformDirectory.resolve("V1__platform.sql"),
                "CREATE TABLE platform_item(id bigint PRIMARY KEY, name text NOT NULL);");
        Files.writeString(businessDirectory.resolve("V1__business.sql"),
                "CREATE TABLE business_item(id bigint PRIMARY KEY, platform_id bigint REFERENCES platform_item(id));");
    }

    @AfterEach
    void tearDown() {
        if (schema != null) jdbc.execute("DROP SCHEMA " + schema + " CASCADE");
    }

    @Test
    void springBootInitializerRunsIndependentV1ChainsBeforeStartupCompletes() {
        context().run(context -> {
            assertNull(context.getStartupFailure());
            // 保留 Boot 唯一的平台 Flyway Bean，业务链不能让平台自动配置退让。
            assertEquals(1, context.getBeansOfType(Flyway.class).size());
            assertEquals(1, versionCount("flyway_schema_history", "1"));
            assertEquals(1, versionCount("flyway_test_domain_schema_history", "1"));
            assertEquals(1, versionCount("flyway_test_domain_schema_history", "0"));
            assertEquals(0, rowCount("business_item"));
        });
    }

    @Test
    void existingPlatformDataSurvivesBusinessBootstrapAndBothChainsAdvanceIndependently() throws Exception {
        Flyway platform = platform();
        platform.migrate();
        jdbc.update("INSERT INTO " + schema + ".platform_item VALUES (1,'用户数据')");
        strategy().migrate(platform);
        jdbc.update("INSERT INTO " + schema + ".business_item VALUES (10,1)");
        // 业务先有 V3，上游随后发布自己的 V2/V3，不受业务最大版本干扰。
        Files.writeString(businessDirectory.resolve("V3__business_extend.sql"),
                "ALTER TABLE business_item ADD COLUMN note text;");
        strategy().migrate(platform);
        Files.writeString(platformDirectory.resolve("V2__platform_extend.sql"),
                "ALTER TABLE platform_item ADD COLUMN description text;");
        Files.writeString(platformDirectory.resolve("V3__platform_index.sql"),
                "CREATE INDEX platform_item_name ON platform_item(name);");
        strategy().migrate(platform);
        strategy().migrate(platform);
        assertEquals(3, rowCount("flyway_schema_history"));
        assertEquals(3, rowCount("flyway_test_domain_schema_history"));
        assertEquals("用户数据", jdbc.queryForObject("SELECT name FROM " + schema + ".platform_item WHERE id=1", String.class));
        assertEquals(1, rowCount("business_item"));
    }

    @Test
    void failedBusinessMigrationStopsStartupAndDoesNotUndoSuccessfulPlatformMigration() throws Exception {
        // 失败 SQL 在 PostgreSQL 事务内回滚；这里只修复尚未成功执行的脚本。
        Path script = businessDirectory.resolve("V1__business.sql");
        Files.writeString(script, "CREATE TABLE business_item(id bigint); SELECT 1 / 0;");
        context().run(context -> assertNotNull(context.getStartupFailure()));
        assertEquals(1, versionCount("flyway_schema_history", "1"));
        assertNull(jdbc.queryForObject("SELECT to_regclass(?)::text", String.class, schema + ".business_item"));
        Files.writeString(script, "CREATE TABLE business_item(id bigint PRIMARY KEY);");
        strategy().migrate(platform());
        assertEquals(1, versionCount("flyway_test_domain_schema_history", "1"));
        assertEquals(1, versionCount("flyway_schema_history", "1"));
    }

    @Test
    void checksumDriftAndOutOfOrderScriptsFailInsteadOfRepairingOrSkipping() throws Exception {
        strategy().migrate(platform());
        Path first = businessDirectory.resolve("V1__business.sql");
        String original = Files.readString(first);
        Files.writeString(first, original + "\n-- 已执行脚本被修改");
        assertThrows(FlywayException.class, () -> strategy().migrate(platform()));
        Files.writeString(first, original);
        Files.writeString(businessDirectory.resolve("V3__later.sql"), "ALTER TABLE business_item ADD COLUMN note text;");
        strategy().migrate(platform());
        Files.writeString(businessDirectory.resolve("V2__late_arrival.sql"), "ALTER TABLE business_item ADD COLUMN missed text;");
        assertThrows(FlywayException.class, () -> strategy().migrate(platform()));
        assertEquals(0, versionCount("flyway_test_domain_schema_history", "2"));
    }

    @Test
    void zeroVersionCannotBeSilentlySkippedByBootstrap() throws Exception {
        Files.writeString(businessDirectory.resolve("V0__invalid.sql"), "CREATE TABLE skipped_item(id bigint);");
        assertThrows(IllegalStateException.class, () -> strategy().migrate(platform()));
        assertNull(jdbc.queryForObject("SELECT to_regclass(?)::text", String.class,
                schema + ".flyway_test_domain_schema_history"));
    }

    private ApplicationContextRunner context() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(FlywayAutoConfiguration.class))
                .withBean(org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy.class, this::strategy)
                .withBean(DataSource.class, () -> dataSource)
                .withPropertyValues("spring.flyway.locations=" + location(platformDirectory),
                        "spring.flyway.default-schema=" + schema,
                        "spring.flyway.schemas=" + schema);
    }

    private Flyway platform() {
        return Flyway.configure().dataSource(dataSource).defaultSchema(schema).schemas(schema)
                .locations(location(platformDirectory)).cleanDisabled(true).validateOnMigrate(true)
                .ignoreMigrationPatterns(new String[0]).load();
    }

    private DomainFlywayMigrationStrategy strategy() {
        return new DomainFlywayMigrationStrategy(new DomainMigration("test", location(businessDirectory), "flyway_test_domain_schema_history", "1"));
    }

    private static String location(Path path) {
        return "filesystem:" + path.toAbsolutePath().toString().replace('\\', '/');
    }

    private int rowCount(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + schema + "." + table, Integer.class);
    }

    private int versionCount(String table, String version) {
        return jdbc.queryForObject("SELECT count(*) FROM " + schema + "." + table + " WHERE version=? AND success", Integer.class, version);
    }
}
