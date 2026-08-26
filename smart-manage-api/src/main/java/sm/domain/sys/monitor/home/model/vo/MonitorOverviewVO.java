package sm.domain.sys.monitor.home.model.vo;

import java.util.List;
import lombok.Data;

@Data
public class MonitorOverviewVO {
  private int hostTelemetryAvailable;
  private int hostTotal;
  private int applicationOnline;
  private int applicationTotal;
  private String databaseHealth;
  private String redisHealth;
  private int pendingCount;
  private int firingCount;
  private int criticalCount;
  private List<AttentionItem> currentAbnormal;
  private List<HostSummary> topology;

  @Data
  public static class AttentionItem {
    private String severity;
    private String ruleCode;
    private String scopeType;
    private String scopeId;
    private String summary;
  }

  @Data
  public static class HostSummary {
    private String hostId;
    private String hostName;
    private String telemetryStatus;
    private int onlineInstances;
    private int totalInstances;
  }
}
