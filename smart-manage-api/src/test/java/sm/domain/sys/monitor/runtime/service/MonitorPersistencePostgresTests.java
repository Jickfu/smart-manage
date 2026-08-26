package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;
import tools.jackson.databind.json.JsonMapper;

/** 在真实 PostgreSQL 上保护历史 UPSERT、聚合和保留语义。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class MonitorPersistencePostgresTests {
  private static final String HOST_ID = "verify-history-host";
  private static final String INSTANCE_A = "verify-history-instance-a";
  private static final String INSTANCE_B = "verify-history-instance-b";
  private static final AtomicLong IDS = new AtomicLong(-9_000_000);

  private JdbcTemplate jdbcTemplate;
  private MonitorSnapshotStore store;
  private MonitorSnapshotSampler sampler;
  private MonitorProperties properties;

  @BeforeEach
  void setUp() {
    DriverManagerDataSource dataSource =
        new DriverManagerDataSource(
            System.getProperty("smartManage.testDbUrl"),
            System.getProperty("smartManage.testDbUser"),
            System.getProperty("smartManage.testDbPassword"));
    jdbcTemplate = new JdbcTemplate(dataSource);
    properties = new MonitorProperties();
    store = new MonitorSnapshotStore(properties);
    sampler =
        new MonitorSnapshotSampler(
            mock(OshiHostMetricsProvider.class),
            mock(ApplicationMetricsProvider.class),
            store,
            mock(StringRedisTemplate.class),
            JsonMapper.builder().build(),
            jdbcTemplate,
            properties);
  }

  @AfterEach
  void cleanUp() {
    jdbcTemplate.update(
        "DELETE FROM t_sys_monitor_instance_history WHERE instance_id IN (?,?)",
        INSTANCE_A,
        INSTANCE_B);
    jdbcTemplate.update("DELETE FROM t_sys_monitor_host_history WHERE host_id=?", HOST_ID);
    jdbcTemplate.update(
        "DELETE FROM t_sys_monitor_alert_notification WHERE incident_id IN (SELECT id FROM"
            + " t_sys_monitor_alert_incident WHERE scope_id LIKE 'verify-topology-%')");
    jdbcTemplate.update(
        "DELETE FROM t_sys_monitor_alert_incident WHERE scope_id LIKE 'verify-topology-%'");
    jdbcTemplate.update(
        "DELETE FROM t_sys_monitor_instance WHERE instance_id LIKE 'verify-topology-%'");
    jdbcTemplate.update("DELETE FROM t_sys_monitor_host WHERE host_id LIKE 'verify-topology-%'");
  }

  @Test
  void historyUpsertKeepsNewestSampleAndSeparatesInstancesOnOneHost() {
    // 固定在同一分钟内，避免测试恰好跨分钟时把两次 UPSERT 写进不同 bucket。
    Instant bucket = Instant.parse("2020-01-02T06:30:30Z");
    // 本用例只验证历史 UPSERT；放宽本地新鲜度，避免固定时刻被当前快照 TTL 排除。
    properties.getSampling().setSnapshotTtlSeconds(3_153_600_000L);
    store.publishHost(host(bucket, .8, "/new", .8));
    store.publishInstance(instance(INSTANCE_A, bucket, .8));
    sampler.persistHistory();
    store.publishHost(host(bucket.minusSeconds(10), .2, "/old", .2));
    store.publishInstance(instance(INSTANCE_A, bucket.minusSeconds(10), .2));
    sampler.persistHistory();
    store.publishInstance(instance(INSTANCE_B, bucket, .6));
    sampler.persistHistory();

    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_host_history WHERE host_id=?",
            Integer.class,
            HOST_ID));
    assertEquals(
        .8,
        jdbcTemplate.queryForObject(
            "SELECT cpu_usage FROM t_sys_monitor_host_history WHERE host_id=?",
            Double.class,
            HOST_ID),
        .000001);
    assertEquals(
        2,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_instance_history WHERE host_id=?",
            Integer.class,
            HOST_ID));
    assertEquals(
        .8,
        jdbcTemplate.queryForObject(
            "SELECT process_cpu FROM t_sys_monitor_instance_history WHERE instance_id=?",
            Double.class,
            INSTANCE_A),
        .000001);
  }

  @Test
  void historyQueryUsesNumericRatiosMatchingWorstMountAndWorstMinuteLatency() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    OffsetDateTime bucketStart =
        now.truncatedTo(ChronoUnit.HOURS).plusMinutes((now.getMinute() / 5L) * 5L).minusMinutes(10);
    insertHost(bucketStart.plusMinutes(1), 5, 10, .95, "/data");
    insertHost(bucketStart.plusMinutes(2), 5, 10, .70, "/backup");
    insertInstance(bucketStart.plusMinutes(1), 768, 1024, 8, 10, 120, 180);
    insertInstance(bucketStart.plusMinutes(2), 768, 1024, 8, 10, 450, 700);
    MonitorHistoryService historyService = new MonitorHistoryService(jdbcTemplate);

    var hostPoint = historyService.history("HOST", HOST_ID, "6h").getFirst();
    var instancePoint = historyService.history("INSTANCE", INSTANCE_A, "6h").getFirst();
    assertEquals(.5, hostPoint.getMemoryUsage(), .000001);
    assertEquals(.95, hostPoint.getFilesystemUsage(), .000001);
    assertEquals("/data", hostPoint.getWorstMount());
    assertEquals(.75, instancePoint.getHeapUsage(), .000001);
    assertEquals(.8, instancePoint.getDbPoolUsage(), .000001);
    assertEquals(450, instancePoint.getP95Ms(), .000001);
    assertEquals(700, instancePoint.getP99Ms(), .000001);
    for (String range : List.of("1h", "6h", "24h", "7d")) {
      assertFalse(historyService.history("HOST", HOST_ID, range).isEmpty());
      assertFalse(historyService.history("INSTANCE", INSTANCE_A, range).isEmpty());
    }
  }

  @Test
  void cleanupDeletesSamplesOutsideRetention() {
    OffsetDateTime old = OffsetDateTime.now(ZoneOffset.UTC).minusDays(8);
    insertHost(old, 5, 10, .5, "/old");
    insertInstance(old, 5, 10, 5, 10, 1, 1);
    properties.getHistory().setRetentionDays(7);

    sampler.cleanupHistory();

    assertEquals(
        0,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_host_history WHERE host_id=?",
            Integer.class,
            HOST_ID));
    assertEquals(
        0,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_instance_history WHERE instance_id=?",
            Integer.class,
            INSTANCE_A));
  }

  @Test
  void retirementClosesIncidentAndSkipsPendingFaultOutboxInPostgres() {
    String instanceId = "verify-topology-retire-notification";
    long incidentId = IDS.decrementAndGet();
    long notificationId = IDS.decrementAndGet();
    long ruleId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM t_sys_monitor_alert_rule WHERE rule_code='INSTANCE_HEAP_HIGH'",
            Long.class);
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_instance
(id,instance_id,host_id,application_name,first_seen_time,last_seen_time,last_start_time,lifecycle)
VALUES(?,?,?,?,now(),now(),now(),'ACTIVE')
""",
        IDS.decrementAndGet(),
        instanceId,
        "verify-topology-host-notification",
        "verify-app");
    jdbcTemplate.update(
        """
        INSERT INTO t_sys_monitor_alert_incident
        (id,rule_id,rule_code,scope_type,scope_id,status,cycle_key,started_at,last_evaluated_at,
         last_value,peak_value,threshold,notification_count,summary,version)
        VALUES(?,?,'INSTANCE_HEAP_HIGH','INSTANCE',?,'FIRING',?,now(),now(),.95,.95,.9,1,'待发送',0)
        """,
        incidentId,
        ruleId,
        instanceId,
        "verify-cycle-" + incidentId);
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_alert_notification
(id,incident_id,notification_type,sequence_no,status,attempt_count,next_attempt_time,create_time)
VALUES(?,?,'FIRING',1,'PENDING',0,now(),now())
""",
        notificationId,
        incidentId);

    new MonitorInstanceLifecycleTxService(jdbcTemplate).retire(instanceId);

    assertEquals(
        "CLOSED",
        jdbcTemplate.queryForObject(
            "SELECT status FROM t_sys_monitor_alert_incident WHERE id=?",
            String.class,
            incidentId));
    assertEquals(
        "SKIPPED",
        jdbcTemplate.queryForObject(
            "SELECT status FROM t_sys_monitor_alert_notification WHERE id=?",
            String.class,
            notificationId));
  }

  @Test
  void currentTopologyExcludesHostWhoseInstancesAreAllRetiredButCatalogKeepsThem() {
    jdbcTemplate.update(
        "INSERT INTO t_sys_monitor_host(id,host_id,host_name,first_seen_time,last_seen_time)"
            + " VALUES(-9200001,'verify-topology-active','active',now(),now()),"
            + "(-9200002,'verify-topology-retired','retired',now(),now())");
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_instance
(id,instance_id,host_id,application_name,application_version,first_seen_time,last_seen_time,last_start_time,lifecycle,retired_at)
VALUES
(-9200011,'verify-topology-active-instance','verify-topology-active','smart-manage','test',now(),now(),now(),'ACTIVE',null),
(-9200012,'verify-topology-retired-instance','verify-topology-retired','smart-manage','test',now(),now(),now(),'RETIRED',now())
""");
    MonitorInstanceRegistry registry = mock(MonitorInstanceRegistry.class);
    when(registry.listOnline()).thenReturn(List.of());
    MonitorSnapshotService snapshots = mock(MonitorSnapshotService.class);
    MonitorTopologyService topology =
        new MonitorTopologyService(
            jdbcTemplate, registry, snapshots, mock(MonitorInstanceLifecycleTxService.class));

    var currentHosts = topology.topology();
    var catalog = topology.catalogInstances();

    assertEquals(
        List.of("verify-topology-active"),
        currentHosts.stream().map(item -> item.getHostId()).toList());
    assertEquals(
        2,
        catalog.stream()
            .filter(item -> item.getInstanceId().startsWith("verify-topology-"))
            .count());
    assertEquals(
        "RETIRED",
        catalog.stream()
            .filter(item -> item.getInstanceId().equals("verify-topology-retired-instance"))
            .findFirst()
            .orElseThrow()
            .getLifecycle());
  }

  private void insertHost(
      OffsetDateTime sampleTime,
      long memoryUsed,
      long memoryTotal,
      double filesystemUsage,
      String mount) {
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_host_history
(id,host_id,sample_bucket,sample_time,memory_total,memory_used,worst_filesystem_usage,worst_mount)
VALUES(?,?,?,?,?,?,?,?)
""",
        IDS.decrementAndGet(),
        HOST_ID,
        sampleTime,
        sampleTime,
        memoryTotal,
        memoryUsed,
        filesystemUsage,
        mount);
  }

  private void insertInstance(
      OffsetDateTime sampleTime,
      long heapUsed,
      long heapMax,
      int active,
      int maximum,
      double p95,
      double p99) {
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_instance_history
(id,instance_id,host_id,sample_bucket,sample_time,heap_used,heap_max,db_active,db_max,http_p95_ms,http_p99_ms)
VALUES(?,?,?,?,?,?,?,?,?,?,?)
""",
        IDS.decrementAndGet(),
        INSTANCE_A,
        HOST_ID,
        sampleTime,
        sampleTime,
        heapUsed,
        heapMax,
        active,
        maximum,
        p95,
        p99);
  }

  private HostSnapshotVO host(Instant sampleTime, double cpu, String mount, double usage) {
    HostSnapshotVO value = new HostSnapshotVO();
    value.setHostId(HOST_ID);
    value.setSampleTime(sampleTime);
    HostSnapshotVO.CpuInfo cpuInfo = new HostSnapshotVO.CpuInfo();
    cpuInfo.setUsage(cpu);
    value.setCpu(cpuInfo);
    HostSnapshotVO.MemoryInfo memory = new HostSnapshotVO.MemoryInfo();
    memory.setCollectorAvailable(true);
    memory.setTotal(10);
    memory.setAvailable(5);
    value.setMemory(memory);
    HostSnapshotVO.FilesystemInfo filesystem = new HostSnapshotVO.FilesystemInfo();
    filesystem.setMount(mount);
    filesystem.setTotal(10);
    filesystem.setUsed(Math.round(usage * 10));
    filesystem.setUsage(usage);
    value.setFilesystems(List.of(filesystem));
    value.setFilesystemsAvailable(true);
    value.setIo(new HostSnapshotVO.IoInfo());
    return value;
  }

  private InstanceSnapshotVO instance(String instanceId, Instant sampleTime, double cpu) {
    InstanceSnapshotVO value = new InstanceSnapshotVO();
    value.setInstanceId(instanceId);
    value.setHostId(HOST_ID);
    value.setSampleTime(sampleTime);
    InstanceSnapshotVO.CpuInfo cpuInfo = new InstanceSnapshotVO.CpuInfo();
    cpuInfo.setProcessUsage(cpu);
    value.setCpu(cpuInfo);
    InstanceSnapshotVO.MemoryInfo memory = new InstanceSnapshotVO.MemoryInfo();
    memory.setCollectorAvailable(true);
    memory.setHeapUsed(768);
    memory.setHeapMax(1024);
    value.setMemory(memory);
    InstanceSnapshotVO.ThreadInfo threads = new InstanceSnapshotVO.ThreadInfo();
    threads.setCollectorAvailable(true);
    threads.setStateCounts(Map.of());
    value.setThreads(threads);
    value.setGc(List.of());
    InstanceSnapshotVO.DataSourceInfo dataSource = new InstanceSnapshotVO.DataSourceInfo();
    dataSource.setCollectorAvailable(true);
    value.setDataSource(dataSource);
    value.setHttp(new InstanceSnapshotVO.HttpInfo());
    InstanceSnapshotVO.HealthInfo health = new InstanceSnapshotVO.HealthInfo();
    health.setCollectorAvailable(true);
    health.setStatus("UP");
    health.setComponents(List.of());
    value.setHealth(health);
    return value;
  }
}
