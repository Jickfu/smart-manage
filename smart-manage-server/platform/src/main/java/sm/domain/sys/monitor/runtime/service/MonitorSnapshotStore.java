package sm.domain.sys.monitor.runtime.service;

import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;

/** Host 与 Instance 独立发布，单个 collector 故障不能阻断另一类遥测。 */
@Component
@RequiredArgsConstructor
class MonitorSnapshotStore {
  private final MonitorProperties properties;
  private final AtomicReference<HostSnapshotVO> currentHost = new AtomicReference<>();
  private final AtomicReference<InstanceSnapshotVO> currentInstance = new AtomicReference<>();

  void publishHost(HostSnapshotVO host) {
    currentHost.set(host);
  }

  void publishInstance(InstanceSnapshotVO instance) {
    currentInstance.set(instance);
  }

  HostSnapshotVO currentHost() {
    HostSnapshotVO snapshot = currentHost.get();
    return isFresh(snapshot == null ? null : snapshot.getSampleTime()) ? snapshot : null;
  }

  InstanceSnapshotVO currentInstance() {
    InstanceSnapshotVO snapshot = currentInstance.get();
    return isFresh(snapshot == null ? null : snapshot.getSampleTime()) ? snapshot : null;
  }

  private boolean isFresh(java.time.Instant sampleTime) {
    if (sampleTime == null) return false;
    long ttlSeconds = properties.getSampling().getSnapshotTtlSeconds();
    return !java.time.Instant.now().isAfter(sampleTime.plusSeconds(ttlSeconds));
  }
}
