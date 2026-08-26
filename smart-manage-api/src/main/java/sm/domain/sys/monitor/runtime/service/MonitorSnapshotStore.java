package sm.domain.sys.monitor.runtime.service;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;

/** 单个采样周期的 Host/Instance 快照必须成对原子发布。 */
@Component
class MonitorSnapshotStore {
  record SnapshotPair(HostSnapshotVO host, InstanceSnapshotVO instance) {}

  private final AtomicReference<SnapshotPair> current = new AtomicReference<>();

  void publish(HostSnapshotVO host, InstanceSnapshotVO instance) {
    current.set(new SnapshotPair(host, instance));
  }

  SnapshotPair current() {
    return current.get();
  }
}
