package sm.domain.sys.monitor.runtime.model.vo;

import java.time.Instant;
import java.util.List;
import lombok.Data;

/** 物理/虚拟主机遥测，不包含任何 JVM 实例指标。 */
@Data
public class HostSnapshotVO {
  private String hostId;
  private String hostname;
  private Instant sampleTime;
  private long uptimeMs;
  private OsInfo os;
  private CpuInfo cpu;
  private MemoryInfo memory;
  private boolean filesystemsAvailable;
  private List<FilesystemInfo> filesystems;
  private IoInfo io;

  @Data
  public static class OsInfo {
    private String name;
    private String version;
    private String arch;
  }

  @Data
  public static class CpuInfo {
    private Double usage;
    private Double loadAverage;
  }

  @Data
  public static class MemoryInfo {
    private boolean collectorAvailable;
    private long total;
    private long available;
    private long swapTotal;
    private long swapUsed;
  }

  @Data
  public static class FilesystemInfo {
    private String name;
    private String mount;
    private String type;
    private long total;
    private long used;
    private long available;
    private Double usage;
  }

  @Data
  public static class IoInfo {
    private boolean collectorAvailable;
    private long diskReadBytes;
    private long diskWriteBytes;
    private Double diskReadBytesPerSecond;
    private Double diskWriteBytesPerSecond;
    private long networkReceiveBytes;
    private long networkTransmitBytes;
    private Double networkReceiveBytesPerSecond;
    private Double networkTransmitBytesPerSecond;
  }
}
