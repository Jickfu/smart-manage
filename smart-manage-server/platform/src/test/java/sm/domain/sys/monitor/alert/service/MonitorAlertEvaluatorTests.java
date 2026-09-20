package sm.domain.sys.monitor.alert.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.HostObservationSourceVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;
import sm.domain.sys.monitor.runtime.service.MonitorSnapshotService;
import sm.domain.sys.monitor.runtime.service.MonitorSnapshotServiceTestFactory;
import sm.domain.sys.monitor.runtime.service.MonitorTopologyService;
import tools.jackson.databind.json.JsonMapper;

class MonitorAlertEvaluatorTests {
  @Test
  void rollingRestartUnknownSourceDoesNotInterruptValidHostCpuEvaluation() throws Exception {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ZSetOperations<String, String> sortedSet = mock(ZSetOperations.class);
    JsonMapper jsonMapper = JsonMapper.builder().build();
    MonitorProperties properties = new MonitorProperties();
    MonitorInstanceRegistry registry = mock(MonitorInstanceRegistry.class);
    when(registry.currentHostId()).thenReturn("host-a");
    when(redisTemplate.opsForValue()).thenReturn(values);
    when(redisTemplate.opsForZSet()).thenReturn(sortedSet);
    when(sortedSet.rangeByScore(
            org.mockito.ArgumentMatchers.eq("sm:monitor:snapshot:host-sources:host-a"),
            org.mockito.ArgumentMatchers.anyDouble(),
            org.mockito.ArgumentMatchers.eq(Double.MAX_VALUE)))
        .thenReturn(new LinkedHashSet<>(List.of("api-01", "api-02")));
    HostObservationSourceVO valid = source("api-01", Instant.now().minusSeconds(5), .95);
    HostObservationSourceVO restarting = source("api-02", Instant.now(), null);
    when(values.multiGet(
            List.of(
                "sm:monitor:snapshot:host-source:host-a:api-01",
                "sm:monitor:snapshot:host-source:host-a:api-02")))
        .thenReturn(
            List.of(
                jsonMapper.writeValueAsString(valid), jsonMapper.writeValueAsString(restarting)));
    MonitorSnapshotService snapshotService =
        MonitorSnapshotServiceTestFactory.create(
            registry, redisTemplate, jsonMapper, jdbcTemplate, properties);
    when(jdbcTemplate.queryForList(
            "SELECT * FROM t_sys_monitor_alert_rule WHERE enabled=true ORDER BY id"))
        .thenReturn(List.of(rule(1L, "HOST_CPU_HIGH", "HOST")));
    MonitorAlertService alertService = mock(MonitorAlertService.class);
    MonitorAlertEvaluator evaluator =
        new MonitorAlertEvaluator(
            jdbcTemplate,
            snapshotService,
            registry,
            mock(MonitorTopologyService.class),
            alertService,
            new MonitorMetricValueFormatter());

    evaluator.evaluate();

    ArgumentCaptor<MonitorAlertEvaluation> evaluation =
        ArgumentCaptor.forClass(MonitorAlertEvaluation.class);
    verify(alertService).evaluateInternal(evaluation.capture());
    assertEquals(0, BigDecimal.valueOf(.95).compareTo(evaluation.getValue().value()));
    verify(alertService, times(0))
        .metricUnavailable(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void redisDownStillEntersDatabaseBackedAlertStateMachine() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    MonitorSnapshotService snapshotService = mock(MonitorSnapshotService.class);
    MonitorAlertService alertService = mock(MonitorAlertService.class);
    MonitorInstanceRegistry registry = mock(MonitorInstanceRegistry.class);
    InstanceSnapshotVO instance = new InstanceSnapshotVO();
    instance.setInstanceId("instance-a");
    InstanceSnapshotVO.HealthComponent redis = new InstanceSnapshotVO.HealthComponent();
    redis.setName("redis");
    redis.setStatus("DOWN");
    InstanceSnapshotVO.HealthInfo health = new InstanceSnapshotVO.HealthInfo();
    health.setCollectorAvailable(true);
    health.setComponents(List.of(redis));
    instance.setHealth(health);
    when(snapshotService.currentInstance()).thenReturn(instance);
    when(registry.currentHostId()).thenReturn("host-a");
    when(jdbcTemplate.queryForList(
            "SELECT * FROM t_sys_monitor_alert_rule WHERE enabled=true ORDER BY id"))
        .thenReturn(
            List.of(
                Map.ofEntries(
                    Map.entry("id", 1L),
                    Map.entry("rule_code", "REDIS_HEALTH_DOWN"),
                    Map.entry("name", "Redis 健康异常"),
                    Map.entry("scope_type", "INSTANCE"),
                    Map.entry("threshold", BigDecimal.ONE),
                    Map.entry("recovery_threshold", BigDecimal.ZERO),
                    Map.entry("duration_seconds", 0),
                    Map.entry("repeat_interval_seconds", 1800),
                    Map.entry("email_enabled", true),
                    Map.entry("value_kind", "BOOLEAN"),
                    Map.entry("display_unit", ""))));
    MonitorAlertEvaluator evaluator =
        new MonitorAlertEvaluator(
            jdbcTemplate,
            snapshotService,
            registry,
            mock(MonitorTopologyService.class),
            alertService,
            new MonitorMetricValueFormatter());

    evaluator.evaluate();

    ArgumentCaptor<MonitorAlertEvaluation> evaluation =
        ArgumentCaptor.forClass(MonitorAlertEvaluation.class);
    verify(alertService).evaluateInternal(evaluation.capture());
    assertEquals("REDIS_HEALTH_DOWN", evaluation.getValue().ruleCode());
    assertEquals(0, BigDecimal.ONE.compareTo(evaluation.getValue().value()));
    assertEquals("Redis 健康异常：instance-a 当前值 异常，阈值 异常", evaluation.getValue().summary());
  }

  @Test
  void hostRulesUseCanonicalSnapshotAndKeepCollectorAvailabilityIndependent() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    MonitorSnapshotService snapshotService = mock(MonitorSnapshotService.class);
    MonitorAlertService alertService = mock(MonitorAlertService.class);
    MonitorInstanceRegistry registry = mock(MonitorInstanceRegistry.class);
    HostSnapshotVO canonicalHost = new HostSnapshotVO();
    canonicalHost.setHostId("host-a");
    HostSnapshotVO.CpuInfo cpu = new HostSnapshotVO.CpuInfo();
    cpu.setUsage(.95);
    canonicalHost.setCpu(cpu);
    canonicalHost.setMemory(new HostSnapshotVO.MemoryInfo());
    InstanceSnapshotVO instance = new InstanceSnapshotVO();
    instance.setInstanceId("instance-a");
    instance.setDataSource(new InstanceSnapshotVO.DataSourceInfo());
    when(registry.currentHostId()).thenReturn("host-a");
    when(snapshotService.currentCanonicalHost("host-a")).thenReturn(canonicalHost);
    when(snapshotService.currentInstance()).thenReturn(instance);
    when(jdbcTemplate.queryForList(
            "SELECT * FROM t_sys_monitor_alert_rule WHERE enabled=true ORDER BY id"))
        .thenReturn(
            List.of(
                rule(1L, "HOST_CPU_HIGH", "HOST"),
                rule(2L, "HOST_MEMORY_HIGH", "HOST"),
                rule(3L, "DB_POOL_HIGH", "INSTANCE")));
    MonitorAlertEvaluator evaluator =
        new MonitorAlertEvaluator(
            jdbcTemplate,
            snapshotService,
            registry,
            mock(MonitorTopologyService.class),
            alertService,
            new MonitorMetricValueFormatter());

    evaluator.evaluate();

    ArgumentCaptor<MonitorAlertEvaluation> evaluation =
        ArgumentCaptor.forClass(MonitorAlertEvaluation.class);
    verify(alertService).evaluateInternal(evaluation.capture());
    assertEquals("HOST_CPU_HIGH", evaluation.getValue().ruleCode());
    assertEquals(0, BigDecimal.valueOf(.95).compareTo(evaluation.getValue().value()));
    verify(alertService).metricUnavailable(2L, "HOST", "host-a");
    verify(alertService).metricUnavailable(3L, "INSTANCE", "instance-a");
  }

  @Test
  void unavailableCanonicalHostUsesMetricUnavailableEvenWhenLocalHostCouldExist() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    MonitorSnapshotService snapshotService = mock(MonitorSnapshotService.class);
    MonitorAlertService alertService = mock(MonitorAlertService.class);
    MonitorInstanceRegistry registry = mock(MonitorInstanceRegistry.class);
    when(registry.currentHostId()).thenReturn("host-a");
    when(snapshotService.currentCanonicalHost("host-a")).thenReturn(null);
    when(jdbcTemplate.queryForList(
            "SELECT * FROM t_sys_monitor_alert_rule WHERE enabled=true ORDER BY id"))
        .thenReturn(List.of(rule(1L, "HOST_CPU_HIGH", "HOST")));
    MonitorAlertEvaluator evaluator =
        new MonitorAlertEvaluator(
            jdbcTemplate,
            snapshotService,
            registry,
            mock(MonitorTopologyService.class),
            alertService,
            new MonitorMetricValueFormatter());

    evaluator.evaluate();

    verify(alertService).metricUnavailable(1L, "HOST", "host-a");
    verify(alertService, times(0)).evaluateInternal(any());
  }

  private Map<String, Object> rule(long id, String code, String scope) {
    return Map.ofEntries(
        Map.entry("id", id),
        Map.entry("rule_code", code),
        Map.entry("name", code),
        Map.entry("scope_type", scope),
        Map.entry("threshold", BigDecimal.ONE),
        Map.entry("recovery_threshold", BigDecimal.ZERO),
        Map.entry("duration_seconds", 0),
        Map.entry("repeat_interval_seconds", 1800),
        Map.entry("email_enabled", true),
        Map.entry("value_kind", "RATIO"),
        Map.entry("display_unit", "%"));
  }

  private HostObservationSourceVO source(String instanceId, Instant sampleTime, Double cpuUsage) {
    HostSnapshotVO snapshot = new HostSnapshotVO();
    snapshot.setHostId("host-a");
    snapshot.setSampleTime(sampleTime);
    HostSnapshotVO.CpuInfo cpu = new HostSnapshotVO.CpuInfo();
    cpu.setUsage(cpuUsage);
    snapshot.setCpu(cpu);
    snapshot.setMemory(new HostSnapshotVO.MemoryInfo());
    snapshot.setFilesystems(List.of());
    snapshot.setIo(new HostSnapshotVO.IoInfo());
    HostObservationSourceVO source = new HostObservationSourceVO();
    source.setHostId("host-a");
    source.setInstanceId(instanceId);
    source.setSnapshot(snapshot);
    return source;
  }
}
