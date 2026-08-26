package sm.domain.sys.monitor.runtime.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;
import tools.jackson.databind.json.JsonMapper;

class MonitorSnapshotSamplerTests {
  @Test
  void hostCollectorFailureDoesNotBlockInstanceSnapshot() {
    OshiHostMetricsProvider hostProvider = mock(OshiHostMetricsProvider.class);
    ApplicationMetricsProvider applicationProvider = mock(ApplicationMetricsProvider.class);
    when(hostProvider.collect(any())).thenThrow(new IllegalStateException("disk unsupported"));
    when(applicationProvider.collect(any())).thenReturn(instance());
    MonitorSnapshotStore store = new MonitorSnapshotStore();
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
    MonitorSnapshotStore store = new MonitorSnapshotStore();
    sampler(hostProvider, applicationProvider, store).sampleCurrent();

    assertNotNull(store.currentHost());
    assertNull(store.currentInstance());
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
        new MonitorProperties());
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
