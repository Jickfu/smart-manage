package sm.domain.sys.monitor.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 内建监控唯一配置入口。 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-manage.domain.sys.monitor")
public class MonitorProperties {
  private String hostId;
  private final Sampling sampling = new Sampling();
  private final History history = new History();
  private final Alert alert = new Alert();
  private final Cluster cluster = new Cluster();

  @Data
  public static class Sampling {
    private long intervalMs = 10000;
    private long snapshotTtlSeconds = 40;
  }

  @Data
  public static class History {
    private long intervalMs = 60000;
    private int retentionDays = 7;
    private String cleanupCron = "0 20 3 * * *";
  }

  @Data
  public static class Alert {
    private long evaluationIntervalMs = 10000;
    private long notificationIntervalMs = 10000;
  }

  @Data
  public static class Cluster {
    private String internalBaseUrl;
    private boolean requireHttps;
    private long heartbeatIntervalMs = 10000;
    private long instanceTtlMs = 30000;
    private long catalogRefreshIntervalMs = 300000;
    private long requestTimeoutMs = 10000;
  }
}
