package sm.domain.sys.monitor.runtime.model.vo;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Data;

@Data
public class MonitorTopologyVO {
  private String hostId;
  private String hostName;
  private String osName;
  private String osVersion;
  private String telemetryStatus;
  private List<Instance> instances;

  @Data
  public static class Instance {
    private String instanceId;
    private String applicationName;
    private String applicationVersion;
    private String lifecycle;
    private boolean online;
    private boolean current;
    private OffsetDateTime lastSeenTime;
    private OffsetDateTime retiredAt;
  }
}
