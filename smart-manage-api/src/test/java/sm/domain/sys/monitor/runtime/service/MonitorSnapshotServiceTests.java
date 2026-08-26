package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
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
            new MonitorSnapshotStore(new sm.domain.sys.monitor.common.config.MonitorProperties()),
            jdbc);

    var telemetry = service.instance("offline-a");

    assertEquals("UNAVAILABLE", telemetry.status());
    assertNull(telemetry.snapshot());
    verify(registry, never()).require("offline-a");
  }
}
