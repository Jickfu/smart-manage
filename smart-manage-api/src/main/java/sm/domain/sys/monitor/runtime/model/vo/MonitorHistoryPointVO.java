package sm.domain.sys.monitor.runtime.model.vo;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class MonitorHistoryPointVO {
  private OffsetDateTime sampleTime;
  private Double cpuUsage;
  private Double memoryUsage;
  private Double filesystemUsage;
  private String worstMount;
  private Double diskReadBytesPerSecond;
  private Double diskWriteBytesPerSecond;
  private Double networkReceiveBytesPerSecond;
  private Double networkTransmitBytesPerSecond;
  private Double processCpuUsage;
  private Double heapUsage;
  private Double requestRate;
  private Double serverErrorRate;
  private Double p95Ms;
  private Double p99Ms;
  private Double threadCount;
  private Double blockedThreadCount;
  private Double dbPoolUsage;
  private Double dbWaiting;
}
