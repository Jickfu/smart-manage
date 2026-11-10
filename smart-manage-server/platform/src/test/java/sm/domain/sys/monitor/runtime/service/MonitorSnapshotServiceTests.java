package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.HostObservationSourceVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;
import tools.jackson.databind.json.JsonMapper;

class MonitorSnapshotServiceTests {
  @Test
  void offlineCatalogInstanceReturnsUnavailableInsteadOfPageLevelFailure() {
    MonitorInstanceRegistry registry = mock(MonitorInstanceRegistry.class);
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    when(redis.opsForValue()).thenReturn(values);
    when(values.get("sm:monitor:snapshot:instance:offline-a")).thenReturn(null);
    when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("offline-a"))).thenReturn(1);
    MonitorSnapshotService service =
        new MonitorSnapshotService(
            registry,
            redis,
            JsonMapper.builder().build(),
            new MonitorSnapshotStore(new MonitorProperties()),
            jdbc,
            new MonitorProperties());

    var telemetry = service.instance("offline-a");

    assertEquals("UNAVAILABLE", telemetry.status());
    assertNull(telemetry.snapshot());
    verify(registry, never()).require("offline-a");
  }

  @Test
  void canonicalHostSelectsFreshestValidObservationForEachMetric() throws Exception {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ZSetOperations<String, String> sortedSet = mock(ZSetOperations.class);
    JsonMapper jsonMapper = JsonMapper.builder().build();
    MonitorProperties properties = new MonitorProperties();
    when(redis.opsForValue()).thenReturn(values);
    when(redis.opsForZSet()).thenReturn(sortedSet);
    MonitorSnapshotService service = service(redis, jsonMapper, properties);
    Instant now = Instant.now();

    configureSources(
        sortedSet,
        values,
        jsonMapper,
        source("host-a", "api-01", now.minusSeconds(5), .95, true, .92),
        source("host-a", "api-02", now, null, false, null));
    HostSnapshotVO canonical = service.currentCanonicalHost("host-a");
    assertEquals(.95, canonical.getCpu().getUsage());
    assertEquals(92L, canonical.getMemory().getTotal() - canonical.getMemory().getAvailable());

    configureSources(
        sortedSet,
        values,
        jsonMapper,
        source("host-a", "api-01", now.minusSeconds(41), .99, true, .99),
        source("host-a", "api-02", now, .88, false, null));
    assertEquals(.88, service.currentCanonicalHost("host-a").getCpu().getUsage());

    // 对外时间只显示到秒，Redis 仍要按同一秒内的实际采样顺序选择。
    Instant sameSecond = now.minusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
    configureSources(
        sortedSet,
        values,
        jsonMapper,
        source("host-a", "api-01", sameSecond.plusMillis(100), .95, true, .90),
        source("host-a", "api-02", sameSecond.plusMillis(900), .97, true, .91));
    canonical = service.currentCanonicalHost("host-a");
    assertEquals(.97, canonical.getCpu().getUsage());
    assertEquals(sameSecond.plusMillis(900), canonical.getSampleTime());

    configureSources(
        sortedSet,
        values,
        jsonMapper,
        source("host-a", "api-01", now.minusSeconds(5), null, false, null),
        source("host-a", "api-02", now, null, false, null));
    canonical = service.currentCanonicalHost("host-a");
    assertNull(canonical.getCpu().getUsage());
    org.junit.jupiter.api.Assertions.assertFalse(canonical.getMemory().isCollectorAvailable());
  }

  @Test
  void canonicalHostRejectsSourceIdentityMismatch() throws Exception {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ZSetOperations<String, String> sortedSet = mock(ZSetOperations.class);
    JsonMapper jsonMapper = JsonMapper.builder().build();
    when(redis.opsForValue()).thenReturn(values);
    when(redis.opsForZSet()).thenReturn(sortedSet);
    configureSources(
        sortedSet,
        values,
        jsonMapper,
        source("host-b", "api-01", Instant.now(), .95, true, .92));

    assertNull(service(redis, jsonMapper, new MonitorProperties()).currentCanonicalHost("host-a"));
  }

  private MonitorSnapshotService service(
      StringRedisTemplate redis, JsonMapper jsonMapper, MonitorProperties properties) {
    return new MonitorSnapshotService(
        mock(MonitorInstanceRegistry.class),
        redis,
        jsonMapper,
        new MonitorSnapshotStore(properties),
        mock(JdbcTemplate.class),
        properties);
  }

  @Test
  void snapshotJsonUsesBeijingWallTimeForManagementAndRoundTrips() {
    var mapper = JsonMapper.builder().build();
    var snapshot = new InstanceSnapshotVO();
    var instant = Instant.parse("2026-12-31T16:30:00Z");
    snapshot.setSampleTime(instant);
    var runtime = new InstanceSnapshotVO.RuntimeInfo();
    runtime.setStartTime(instant);
    snapshot.setRuntime(runtime);

    var json = mapper.valueToTree(snapshot);
    assertEquals(
        "2027-01-01 00:30:00", json.get("sampleTime").asText());
    assertEquals(
        "2027-01-01 00:30:00", json.get("runtime").get("startTime").asText());
    assertEquals(
        instant, mapper.treeToValue(json, InstanceSnapshotVO.class).getSampleTime());
  }

  private HostSnapshotVO host(String hostId, Instant sampleTime) {
    HostSnapshotVO snapshot = new HostSnapshotVO();
    snapshot.setHostId(hostId);
    snapshot.setSampleTime(sampleTime);
    return snapshot;
  }

  private HostObservationSourceVO source(
      String hostId,
      String instanceId,
      Instant sampleTime,
      Double cpuUsage,
      boolean memoryAvailable,
      Double memoryUsage) {
    HostSnapshotVO snapshot = host(hostId, sampleTime);
    HostSnapshotVO.CpuInfo cpu = new HostSnapshotVO.CpuInfo();
    cpu.setUsage(cpuUsage);
    snapshot.setCpu(cpu);
    HostSnapshotVO.MemoryInfo memory = new HostSnapshotVO.MemoryInfo();
    memory.setCollectorAvailable(memoryAvailable);
    if (memoryUsage != null) {
      memory.setTotal(100);
      memory.setAvailable(Math.round(100 * (1 - memoryUsage)));
    }
    snapshot.setMemory(memory);
    snapshot.setFilesystems(List.of());
    snapshot.setIo(new HostSnapshotVO.IoInfo());
    HostObservationSourceVO source = new HostObservationSourceVO();
    source.setHostId(hostId);
    source.setInstanceId(instanceId);
    source.setObservedAt(snapshot.getSampleTime());
    source.setSnapshot(snapshot);
    return source;
  }

  private void configureSources(
      ZSetOperations<String, String> sortedSet,
      ValueOperations<String, String> values,
      JsonMapper jsonMapper,
      HostObservationSourceVO... sources)
      throws Exception {
    LinkedHashSet<String> instanceIds = new LinkedHashSet<>();
    java.util.Map<String, String> valuesByKey = new java.util.HashMap<>();
    for (HostObservationSourceVO source : sources) {
      instanceIds.add(source.getInstanceId());
      valuesByKey.put(
          MonitorSnapshotService.hostSourceKey("host-a", source.getInstanceId()),
          jsonMapper.writeValueAsString(source));
    }
    when(sortedSet.rangeByScore(
            eq(MonitorSnapshotService.hostSourceIndexKey("host-a")),
            anyDouble(),
            eq(Double.MAX_VALUE)))
        .thenReturn(instanceIds);
    when(values.multiGet(org.mockito.ArgumentMatchers.anyList()))
        .thenAnswer(
            invocation -> {
              List<String> keys = invocation.getArgument(0);
              return keys.stream().map(valuesByKey::get).toList();
            });
  }
}
