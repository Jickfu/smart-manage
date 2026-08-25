package sm.domain.sys.monitor.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 使用真实 PostgreSQL 驱动验证 timestamptz 参数绑定和目录 UPSERT。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class MonitorCatalogAccessorPostgresTests {
    private static final String INSTANCE_ID = "verify-monitor-instance";
    private static final String HOST_ID = "verify-monitor-host";

    @Test
    void touchBindsPostgresTimestampWithTimeZone() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                System.getProperty("smartManage.testDbUrl"),
                System.getProperty("smartManage.testDbUser"),
                System.getProperty("smartManage.testDbPassword"));
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        MonitorCatalogAccessor accessor = new MonitorCatalogAccessor(jdbcTemplate);
        MonitorInstanceRegistry.RegisteredInstance instance = new MonitorInstanceRegistry.RegisteredInstance();
        instance.setInstanceId(INSTANCE_ID);
        instance.setHostId(HOST_ID);
        instance.setHostName("verify-host");
        instance.setOsName("Windows");
        instance.setOsVersion("test");
        instance.setArch("amd64");
        instance.setApplicationName("smart-manage");
        instance.setApplicationVersion("test");
        instance.setStartTime(System.currentTimeMillis() - 1000);
        instance.setLastSeenTime(System.currentTimeMillis());

        try {
            accessor.touch(instance);
            assertEquals(1, jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM t_sys_monitor_instance WHERE instance_id=?", Integer.class, INSTANCE_ID));
        } finally {
            jdbcTemplate.update("DELETE FROM t_sys_monitor_instance WHERE instance_id=?", INSTANCE_ID);
            jdbcTemplate.update("DELETE FROM t_sys_monitor_host WHERE host_id=?", HOST_ID);
        }
    }
}
