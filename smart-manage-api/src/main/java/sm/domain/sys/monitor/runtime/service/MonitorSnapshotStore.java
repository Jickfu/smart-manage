package sm.domain.sys.monitor.runtime.service;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;

/** Host 与 Instance 独立发布，单个 collector 故障不能阻断另一类遥测。 */
@Component
class MonitorSnapshotStore {
  private final AtomicReference<HostSnapshotVO> currentHost = new AtomicReference<>();
  private final AtomicReference<InstanceSnapshotVO> currentInstance = new AtomicReference<>();

  void publishHost(HostSnapshotVO host) {
    currentHost.set(host);
  }

  void publishInstance(InstanceSnapshotVO instance) {
    currentInstance.set(instance);
  }

  HostSnapshotVO currentHost() {
    return currentHost.get();
  }

  InstanceSnapshotVO currentInstance() {
    return currentInstance.get();
  }
}
