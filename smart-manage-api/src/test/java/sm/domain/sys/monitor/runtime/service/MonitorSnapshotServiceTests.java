package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
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
  void canonicalHostRequiresFreshSampleAndMatchingHostIdentity() throws Exception {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    JsonMapper jsonMapper = mock(JsonMapper.class);
    MonitorProperties properties = new MonitorProperties();
    when(redis.opsForValue()).thenReturn(values);
    when(values.get("sm:monitor:snapshot:host:host-a")).thenReturn("fresh");
    HostSnapshotVO fresh = host("host-a", Instant.now());
    when(jsonMapper.readValue("fresh", HostSnapshotVO.class)).thenReturn(fresh);
    MonitorSnapshotService service = service(redis, jsonMapper, properties);

    assertNotNull(service.currentCanonicalHost("host-a"));

    HostSnapshotVO stale =
        host("host-a", Instant.now().minusSeconds(properties.getSampling().getSnapshotTtlSeconds() + 1));
    when(jsonMapper.readValue("fresh", HostSnapshotVO.class)).thenReturn(stale);
    assertNull(service.currentCanonicalHost("host-a"));

    when(jsonMapper.readValue("fresh", HostSnapshotVO.class))
        .thenReturn(host("host-b", Instant.now()));
    assertNull(service.currentCanonicalHost("host-a"));
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

  private HostSnapshotVO host(String hostId, Instant sampleTime) {
    HostSnapshotVO snapshot = new HostSnapshotVO();
    snapshot.setHostId(hostId);
    snapshot.setSampleTime(sampleTime);
    return snapshot;
  }
}
