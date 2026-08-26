package sm.domain.sys.monitor.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** 使用真实 PostgreSQL 驱动验证 timestamptz 参数绑定和目录 UPSERT。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class MonitorCatalogAccessorPostgresTests {
  private static final String INSTANCE_ID = "verify-monitor-instance";
  private static final String HOST_ID = "verify-monitor-host";

  @Test
  void touchBindsPostgresTimestampWithTimeZone() {
    DriverManagerDataSource dataSource =
        new DriverManagerDataSource(
            System.getProperty("smartManage.testDbUrl"),
            System.getProperty("smartManage.testDbUser"),
            System.getProperty("smartManage.testDbPassword"));
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    MonitorCatalogAccessor accessor = new MonitorCatalogAccessor(jdbcTemplate);
    MonitorInstanceRegistry.RegisteredInstance instance =
        new MonitorInstanceRegistry.RegisteredInstance();
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
      assertEquals(
          1,
          jdbcTemplate.queryForObject(
              "SELECT count(*) FROM t_sys_monitor_instance WHERE instance_id=?",
              Integer.class,
              INSTANCE_ID));
    } finally {
      jdbcTemplate.update("DELETE FROM t_sys_monitor_instance WHERE instance_id=?", INSTANCE_ID);
      jdbcTemplate.update("DELETE FROM t_sys_monitor_host WHERE host_id=?", HOST_ID);
    }
  }

  @Test
  void catalogsOneHostManyInstancesAndManyHostsAndReactivatesRetiredInstance() {
    DriverManagerDataSource dataSource =
        new DriverManagerDataSource(
            System.getProperty("smartManage.testDbUrl"),
            System.getProperty("smartManage.testDbUser"),
            System.getProperty("smartManage.testDbPassword"));
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    MonitorCatalogAccessor accessor = new MonitorCatalogAccessor(jdbc);
    String[] instanceIds = {"verify-monitor-a1", "verify-monitor-a2", "verify-monitor-b1"};
    try {
      accessor.touch(instance(instanceIds[0], "verify-host-a"));
      accessor.touch(instance(instanceIds[1], "verify-host-a"));
      accessor.touch(instance(instanceIds[2], "verify-host-b"));
      assertEquals(
          2,
          jdbc.queryForObject(
              "SELECT count(*) FROM t_sys_monitor_instance WHERE host_id='verify-host-a'",
              Integer.class));
      assertEquals(
          2,
          jdbc.queryForObject(
              "SELECT count(DISTINCT host_id) FROM t_sys_monitor_instance WHERE instance_id LIKE"
                  + " 'verify-monitor-%'",
              Integer.class));
      jdbc.update(
          "UPDATE t_sys_monitor_instance SET lifecycle='RETIRED',retired_at=now() WHERE"
              + " instance_id=?",
          instanceIds[0]);
      accessor.reactivateIfRetired(instanceIds[0]);
      assertEquals(
          "ACTIVE",
          jdbc.queryForObject(
              "SELECT lifecycle FROM t_sys_monitor_instance WHERE instance_id=?",
              String.class,
              instanceIds[0]));
    } finally {
      for (String id : instanceIds)
        jdbc.update("DELETE FROM t_sys_monitor_instance WHERE instance_id=?", id);
      jdbc.update(
          "DELETE FROM t_sys_monitor_host WHERE host_id IN ('verify-host-a','verify-host-b')");
    }
  }

  private MonitorInstanceRegistry.RegisteredInstance instance(String instanceId, String hostId) {
    MonitorInstanceRegistry.RegisteredInstance value =
        new MonitorInstanceRegistry.RegisteredInstance();
    value.setInstanceId(instanceId);
    value.setHostId(hostId);
    value.setHostName(hostId);
    value.setOsName("Windows");
    value.setOsVersion("test");
    value.setArch("amd64");
    value.setApplicationName("smart-manage");
    value.setApplicationVersion("test");
    value.setStartTime(System.currentTimeMillis() - 1000);
    value.setLastSeenTime(System.currentTimeMillis());
    return value;
  }
}
