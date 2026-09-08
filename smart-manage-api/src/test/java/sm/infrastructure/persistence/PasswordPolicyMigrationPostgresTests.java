package sm.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** 使用专属临时库验证 V3、V4 带数据升级；不对开发库或生产库执行建库清理。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class PasswordPolicyMigrationPostgresTests {
    @Test
    void upgradePreservesUsersAndDictionaryEditsAcrossRestart() {
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
            Flyway.configure().dataSource(source).locations("classpath:db/migration")
                    .target("3").cleanDisabled(true).load().migrate();
            var jdbc = new JdbcTemplate(source);
            jdbc.update("""
                    INSERT INTO t_sys_user (id, username, number, name, password, password_reset, enabled)
                    VALUES (9200000901, 'upgrade-preserved-user', 'upgrade-preserved-user', '升级保留用户',
                        'existing-opaque-hash', false, false)
                    """);
            var before = jdbc.queryForMap("SELECT * FROM t_sys_user WHERE id = 9200000901");
            var administrator = jdbc.queryForMap("SELECT password, credential_generation FROM t_sys_user WHERE id = 1");
            var previousHistory = jdbc.queryForList("SELECT * FROM flyway_schema_history WHERE version IN ('1', '2', '3') ORDER BY installed_rank");
            Flyway flyway = Flyway.configure().dataSource(source).locations("classpath:db/migration")
                    .cleanDisabled(true).validateOnMigrate(true).load();
            Flyway.configure().dataSource(source).locations("classpath:db/migration")
                    .target("4").cleanDisabled(true).load().migrate();
            assertEquals(before, jdbc.queryForMap("SELECT * FROM t_sys_user WHERE id = 9200000901"));
            assertEquals(administrator.get("password"), jdbc.queryForObject("SELECT password FROM t_sys_user WHERE id = 1", String.class));
            assertEquals(true, jdbc.queryForObject("SELECT password_reset FROM t_sys_user WHERE id = 1", Boolean.class));
            assertEquals(((Number) administrator.get("credential_generation")).longValue() + 1,
                    jdbc.queryForObject("SELECT credential_generation FROM t_sys_user WHERE id = 1", Long.class));
            assertEquals(1002, jdbc.queryForObject("SELECT count(*) FROM t_sys_weak_password", Integer.class));
            assertEquals(previousHistory, jdbc.queryForList("SELECT * FROM flyway_schema_history WHERE version IN ('1', '2', '3') ORDER BY installed_rank"));
            jdbc.update("DELETE FROM t_sys_weak_password WHERE id = 560000000000010000");
            var dictionaryBeforeV5 = jdbc.queryForList("SELECT * FROM t_sys_weak_password ORDER BY id");
            var menuBeforeV5 = jdbc.queryForMap("SELECT number, app_id, feature_id, permission_id, component FROM t_sys_menu WHERE id = 560000000000000110");
            var v4History = jdbc.queryForList("SELECT * FROM flyway_schema_history ORDER BY installed_rank");
            assertEquals(470000000000000001L, jdbc.queryForObject("SELECT parent_id FROM t_sys_menu WHERE id = 560000000000000110", Long.class));
            Flyway.configure().dataSource(source).locations("classpath:db/migration")
                    .target("5").cleanDisabled(true).load().migrate();
            assertEquals(470000000000000005L, jdbc.queryForObject("SELECT parent_id FROM t_sys_menu WHERE id = 560000000000000110", Long.class));
            assertEquals(menuBeforeV5, jdbc.queryForMap("SELECT number, app_id, feature_id, permission_id, component FROM t_sys_menu WHERE id = 560000000000000110"));
            assertEquals(dictionaryBeforeV5, jdbc.queryForList("SELECT * FROM t_sys_weak_password ORDER BY id"));
            assertEquals(v4History, jdbc.queryForList("SELECT * FROM flyway_schema_history WHERE version IN ('1', '2', '3', '4') ORDER BY installed_rank"));
            var menuAfterV5 = jdbc.queryForMap("SELECT * FROM t_sys_menu WHERE id = 560000000000000110");
            // V6 仅清理初始化占位描述，真实维护内容和词条匹配身份必须保留。
            jdbc.update("UPDATE t_sys_weak_password SET description = '管理员补充的真实说明' WHERE id = 560000000000010001");
            var maintainedWord = jdbc.queryForMap("SELECT * FROM t_sys_weak_password WHERE id = 560000000000010001");
            var wordIdentities = jdbc.queryForList("SELECT id, word, match_digest FROM t_sys_weak_password ORDER BY id");
            flyway.migrate();
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM t_sys_weak_password WHERE description = '初始弱口令词库'", Integer.class));
            assertEquals(1000, jdbc.queryForObject("SELECT count(*) FROM t_sys_weak_password WHERE description IS NULL", Integer.class));
            assertEquals(maintainedWord, jdbc.queryForMap("SELECT * FROM t_sys_weak_password WHERE id = 560000000000010001"));
            assertEquals(wordIdentities, jdbc.queryForList("SELECT id, word, match_digest FROM t_sys_weak_password ORDER BY id"));
            var dictionaryAfterV6 = jdbc.queryForList("SELECT * FROM t_sys_weak_password ORDER BY id");
            flyway.migrate();
            assertEquals(dictionaryAfterV6, jdbc.queryForList("SELECT * FROM t_sys_weak_password ORDER BY id"));
            assertEquals(menuAfterV5, jdbc.queryForMap("SELECT * FROM t_sys_menu WHERE id = 560000000000000110"));
            assertEquals(1001, jdbc.queryForObject("SELECT count(*) FROM t_sys_weak_password", Integer.class));
            assertEquals(before, jdbc.queryForMap("SELECT * FROM t_sys_user WHERE id = 9200000901"));
        } finally {
            // 名称由固定前缀与 UUID 构成，仅 CREATE 明确成功后才删除本测试拥有的数据库。
            if (created) verification.execute("DROP DATABASE " + database);
        }
    }
}
