package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.HostObservationSourceVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;
import tools.jackson.databind.json.JsonMapper;

class MonitorSnapshotSamplerTests {
  @Test
  void publishesHostObservationUnderIndependentInstanceSourceKey() throws Exception {
    OshiHostMetricsProvider hostProvider = mock(OshiHostMetricsProvider.class);
    ApplicationMetricsProvider applicationProvider = mock(ApplicationMetricsProvider.class);
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ZSetOperations<String, String> sortedSet = mock(ZSetOperations.class);
    MonitorInstanceRegistry registry = mock(MonitorInstanceRegistry.class);
    JsonMapper jsonMapper = JsonMapper.builder().build();
    HostSnapshotVO host = host();
    when(hostProvider.collect(any())).thenReturn(host);
    when(applicationProvider.collect(any())).thenThrow(new IllegalStateException("unavailable"));
    when(redisTemplate.opsForValue()).thenReturn(values);
    when(redisTemplate.opsForZSet()).thenReturn(sortedSet);
    when(registry.currentInstanceId()).thenReturn("api-01");
    MonitorProperties properties = new MonitorProperties();
    MonitorSnapshotSampler sampler =
        new MonitorSnapshotSampler(
            hostProvider,
            applicationProvider,
            new MonitorSnapshotStore(properties),
            redisTemplate,
            jsonMapper,
            mock(JdbcTemplate.class),
            properties,
            registry);

    sampler.sampleCurrent();

    var jsonCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
    verify(values)
        .set(
            eq("sm:monitor:snapshot:host-source:host-a:api-01"),
            jsonCaptor.capture(),
            eq(Duration.ofSeconds(properties.getSampling().getSnapshotTtlSeconds())));
    HostObservationSourceVO published =
        jsonMapper.readValue(jsonCaptor.getValue(), HostObservationSourceVO.class);
    assertNotNull(published.getSnapshot());
    org.junit.jupiter.api.Assertions.assertEquals("host-a", published.getHostId());
    org.junit.jupiter.api.Assertions.assertEquals("api-01", published.getInstanceId());
    verify(sortedSet)
        .add(
            "sm:monitor:snapshot:host-sources:host-a",
            "api-01",
            host.getSampleTime().toEpochMilli());
  }

  @Test
  void hostCollectorFailureDoesNotBlockInstanceSnapshot() {
    OshiHostMetricsProvider hostProvider = mock(OshiHostMetricsProvider.class);
    ApplicationMetricsProvider applicationProvider = mock(ApplicationMetricsProvider.class);
    when(hostProvider.collect(any())).thenThrow(new IllegalStateException("disk unsupported"));
    when(applicationProvider.collect(any())).thenReturn(instance());
    MonitorSnapshotStore store = new MonitorSnapshotStore(new MonitorProperties());
    sampler(hostProvider, applicationProvider, store).sampleCurrent();

    assertNull(store.currentHost());
    assertNotNull(store.currentInstance());
  }

  @Test
  void instanceCollectorFailureDoesNotBlockHostSnapshot() {
    OshiHostMetricsProvider hostProvider = mock(OshiHostMetricsProvider.class);
    ApplicationMetricsProvider applicationProvider = mock(ApplicationMetricsProvider.class);
    when(hostProvider.collect(any())).thenReturn(host());
    when(applicationProvider.collect(any())).thenThrow(new IllegalStateException("health failed"));
    MonitorSnapshotStore store = new MonitorSnapshotStore(new MonitorProperties());
    sampler(hostProvider, applicationProvider, store).sampleCurrent();

    assertNotNull(store.currentHost());
    assertNull(store.currentInstance());
  }

  @Test
  void staleSnapshotsAreUnavailableAndAreNotPersistedAgain() {
    MonitorProperties properties = new MonitorProperties();
    properties.getSampling().setSnapshotTtlSeconds(1);
    MonitorSnapshotStore store = new MonitorSnapshotStore(properties);
    HostSnapshotVO host = host();
    host.setSampleTime(Instant.now().minusSeconds(2));
    InstanceSnapshotVO instance = instance();
    instance.setSampleTime(Instant.now().minusSeconds(2));
    store.publishHost(host);
    store.publishInstance(instance);
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    MonitorSnapshotSampler sampler =
        new MonitorSnapshotSampler(
            mock(OshiHostMetricsProvider.class),
            mock(ApplicationMetricsProvider.class),
            store,
            mock(StringRedisTemplate.class),
            JsonMapper.builder().build(),
            jdbcTemplate,
            properties,
            mock(MonitorInstanceRegistry.class));

    assertNull(store.currentHost());
    assertNull(store.currentInstance());
    sampler.persistHistory();
    verifyNoInteractions(jdbcTemplate);
  }

  private MonitorSnapshotSampler sampler(
      OshiHostMetricsProvider hostProvider,
      ApplicationMetricsProvider applicationProvider,
      MonitorSnapshotStore store) {
    return new MonitorSnapshotSampler(
        hostProvider,
        applicationProvider,
        store,
        mock(StringRedisTemplate.class),
        JsonMapper.builder().build(),
        mock(JdbcTemplate.class),
        new MonitorProperties(),
        mock(MonitorInstanceRegistry.class));
  }

  private HostSnapshotVO host() {
    HostSnapshotVO value = new HostSnapshotVO();
    value.setHostId("host-a");
    value.setSampleTime(Instant.now());
    return value;
  }

  private InstanceSnapshotVO instance() {
    InstanceSnapshotVO value = new InstanceSnapshotVO();
    value.setInstanceId("instance-a");
    value.setSampleTime(Instant.now());
    return value;
  }
}
