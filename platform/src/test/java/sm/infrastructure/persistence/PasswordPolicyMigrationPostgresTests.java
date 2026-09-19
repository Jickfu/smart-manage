package sm.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** 使用专属临时库验证最终基线及重启数据保留；不对开发库或生产库执行建库清理。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class PasswordPolicyMigrationPostgresTests {
    @Test
    void baselineInitializesFinalStateAndPreservesEditsAcrossRestart() {
        String url = System.getProperty("smartManage.testDbUrl");
        String username = System.getProperty("smartManage.testDbUser");
        String password = System.getProperty("smartManage.testDbPassword");
        var verification = new JdbcTemplate(new DriverManagerDataSource(url, username, password));
        String currentDatabase = verification.queryForObject("SELECT current_database()", String.class);
        assertNotNull(currentDatabase);
        assertTrue(currentDatabase.startsWith("smart_manage_verify_"), "只允许从空库验证的临时库创建升级测试库");
        String database = "smart_manage_password_upgrade_" + UUID.randomUUID().toString().replace("-", "");
        String upgradeUrl = url.substring(0, url.lastIndexOf('/') + 1) + database;
        boolean created = false;
        try {
            verification.execute("CREATE DATABASE " + database);
            created = true;
            var source = new DriverManagerDataSource(upgradeUrl, username, password);
            Flyway.configure().dataSource(source).locations("classpath:db/platform/migration")
                    .target("2").cleanDisabled(true).load().migrate();
            var jdbc = new JdbcTemplate(source);
            jdbc.update("""
                    INSERT INTO t_sys_user (id, username, number, name, password, password_reset, enabled)
                    VALUES (9200000901, 'upgrade-preserved-user', 'upgrade-preserved-user', '升级保留用户',
                        'existing-opaque-hash', false, false)
                    """);
            var before = jdbc.queryForMap("SELECT * FROM t_sys_user WHERE id = 9200000901");
            assertEquals(true, jdbc.queryForObject("SELECT password_reset FROM t_sys_user WHERE id = 1", Boolean.class));
            assertEquals(1002, jdbc.queryForObject("SELECT count(*) FROM t_sys_weak_password", Integer.class));
            assertEquals(1002, jdbc.queryForObject("SELECT count(*) FROM t_sys_weak_password WHERE description IS NULL", Integer.class));
            assertEquals(470000000000000005L, jdbc.queryForObject("SELECT parent_id FROM t_sys_menu WHERE id = 560000000000000110", Long.class));
            assertEquals(60, jdbc.queryForObject("SELECT sort FROM t_sys_menu WHERE id = 560000000000000110", Integer.class));
            assertEquals("LockOutlined", jdbc.queryForObject("SELECT icon FROM t_sys_menu WHERE id = 560000000000000110", String.class));
            assertEquals(5, jdbc.queryForObject("SELECT count(*) FROM t_sys_job WHERE app_id IS NOT NULL", Integer.class));
            assertEquals(0, jdbc.queryForObject("""
                    SELECT count(*) FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name LIKE 't_sys_job%' AND column_name = 'job_group'
                    """, Integer.class));
            assertEquals(java.util.List.of("1", "2"), jdbc.queryForList(
                    "SELECT version FROM flyway_schema_history ORDER BY installed_rank", String.class));
            // 再次迁移不得恢复管理员删除的词条，也不得覆盖真实维护内容。
            jdbc.update("DELETE FROM t_sys_weak_password WHERE id = 560000000000010000");
            jdbc.update("UPDATE t_sys_weak_password SET description = '管理员补充的真实说明' WHERE id = 560000000000010001");
            jdbc.update("UPDATE t_sys_menu SET icon = 'SettingOutlined' WHERE id = 560000000000000110");
            var dictionary = jdbc.queryForList("SELECT * FROM t_sys_weak_password ORDER BY id");
            var menu = jdbc.queryForMap("SELECT * FROM t_sys_menu WHERE id = 560000000000000110");
            var administrator = jdbc.queryForMap("SELECT * FROM t_sys_user WHERE id = 1");
            var history = jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank");
            Flyway flyway = Flyway.configure().dataSource(source).locations("classpath:db/platform/migration")
                    .cleanDisabled(true).validateOnMigrate(true).load();
            flyway.migrate();
            flyway.migrate();
            assertEquals(history, jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank"));
            assertEquals(menu, jdbc.queryForMap("SELECT * FROM t_sys_menu WHERE id = 560000000000000110"));
            assertEquals(dictionary, jdbc.queryForList("SELECT * FROM t_sys_weak_password ORDER BY id"));
            assertEquals(administrator, jdbc.queryForMap("SELECT * FROM t_sys_user WHERE id = 1"));
            assertEquals(before, jdbc.queryForMap("SELECT * FROM t_sys_user WHERE id = 9200000901"));
        } finally {
            // 名称由固定前缀与 UUID 构成，仅 CREATE 明确成功后才删除本测试拥有的数据库。
            if (created) verification.execute("DROP DATABASE " + database);
        }
    }
}
