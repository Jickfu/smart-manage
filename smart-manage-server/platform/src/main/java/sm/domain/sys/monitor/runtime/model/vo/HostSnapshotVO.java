package sm.domain.sys.monitor.runtime.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import lombok.Data;

/**
 * 物理/虚拟主机遥测，不包含任何 JVM 实例指标。
 */
@Schema(description = "主机遥测快照输出模型")
@Data
public class HostSnapshotVO {
  @Schema(description = "运行主机稳定标识")
  private String hostId;
  @Schema(description = "主机名称")
  private String hostname;
  @Schema(description = "采样时间")
  private Instant sampleTime;
  @Schema(description = "运行时长，单位毫秒")
  private long uptimeMs;
  @Schema(description = "操作系统信息")
  private OsInfo os;
  @Schema(description = "CPU 指标快照")
  private CpuInfo cpu;
  @Schema(description = "内存指标快照")
  private MemoryInfo memory;
  @Schema(description = "文件系统采集器本次是否可用")
  private boolean filesystemsAvailable;
  @Schema(description = "文件系统指标列表")
  private List<FilesystemInfo> filesystems;
  @Schema(description = "主机磁盘和网络 IO 指标")
  private IoInfo io;

  @Schema(description = "操作系统信息")
  @Data
  public static class OsInfo {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "操作系统版本")
    private String version;
    @Schema(description = "系统架构")
    private String arch;
  }

  @Schema(description = "CPU 指标")
  @Data
  public static class CpuInfo {
    @Schema(description = "使用率，原始比值 0～1；不可用时为 null")
    private Double usage;
    @Schema(description = "最近一分钟系统平均负载，不是 CPU 使用率；不可用时为 null")
    private Double loadAverage;
  }

  @Schema(description = "内存指标")
  @Data
  public static class MemoryInfo {
    @Schema(description = "该采集器本次是否可用；为 false 时其数值不能解释为有效零值")
    private boolean collectorAvailable;
    @Schema(description = "主机物理内存总量，单位字节；仅在 collectorAvailable 为 true 时有效")
    private long total;
    @Schema(description = "主机可用物理内存，单位字节；仅在 collectorAvailable 为 true 时有效")
    private long available;
    @Schema(description = "交换区总字节数")
    private long swapTotal;
    @Schema(description = "交换区已用字节数")
    private long swapUsed;
  }

  @Schema(description = "文件系统指标")
  @Data
  public static class FilesystemInfo {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "文件系统挂载点")
    private String mount;
    @Schema(description = "文件系统类型")
    private String type;
    @Schema(description = "文件系统总容量，单位字节")
    private long total;
    @Schema(description = "已使用容量，单位字节")
    private long used;
    @Schema(description = "文件系统可用容量，单位字节")
    private long available;
    @Schema(description = "使用率，原始比值 0～1；不可用时为 null")
    private Double usage;
  }

  @Schema(description = "磁盘与网络 IO 指标")
  @Data
  public static class IoInfo {
    @Schema(description = "该采集器本次是否可用；为 false 时其数值不能解释为有效零值")
    private boolean collectorAvailable;
    @Schema(description = "磁盘累计读取字节数")
    private long diskReadBytes;
    @Schema(description = "磁盘累计写入字节数")
    private long diskWriteBytes;
    @Schema(description = "磁盘读取速率，单位字节/秒；不可用时为 null")
    private Double diskReadBytesPerSecond;
    @Schema(description = "磁盘写入速率，单位字节/秒；不可用时为 null")
    private Double diskWriteBytesPerSecond;
    @Schema(description = "网络累计接收字节数")
    private long networkReceiveBytes;
    @Schema(description = "网络累计发送字节数")
    private long networkTransmitBytes;
    @Schema(description = "网络接收速率，单位字节/秒；不可用时为 null")
    private Double networkReceiveBytesPerSecond;
    @Schema(description = "网络发送速率，单位字节/秒；不可用时为 null")
    private Double networkTransmitBytesPerSecond;
  }
}
