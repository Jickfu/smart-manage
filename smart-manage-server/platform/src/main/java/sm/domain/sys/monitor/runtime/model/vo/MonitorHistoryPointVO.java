package sm.domain.sys.monitor.runtime.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import lombok.Data;

@Schema(description = "监控历史采样点输出模型")
@Data
public class MonitorHistoryPointVO {
  @Schema(description = "采样时间")
  private OffsetDateTime sampleTime;
  @Schema(description = "主机 CPU 使用率，原始比值 0～1；不可用时为 null")
  private Double cpuUsage;
  @Schema(description = "主机内存使用率，原始比值 0～1；不可用时为 null")
  private Double memoryUsage;
  @Schema(description = "文件系统最高使用率，原始比值 0～1；不可用时为 null")
  private Double filesystemUsage;
  @Schema(description = "最高使用率挂载点")
  private String worstMount;
  @Schema(description = "磁盘读取速率，单位字节/秒；不可用时为 null")
  private Double diskReadBytesPerSecond;
  @Schema(description = "磁盘写入速率，单位字节/秒；不可用时为 null")
  private Double diskWriteBytesPerSecond;
  @Schema(description = "网络接收速率，单位字节/秒；不可用时为 null")
  private Double networkReceiveBytesPerSecond;
  @Schema(description = "网络发送速率，单位字节/秒；不可用时为 null")
  private Double networkTransmitBytesPerSecond;
  @Schema(description = "JVM 进程 CPU 使用率，原始比值 0～1；不可用时为 null")
  private Double processCpuUsage;
  @Schema(description = "JVM 堆内存使用率，原始比值 0～1；不可用时为 null")
  private Double heapUsage;
  @Schema(description = "HTTP 请求速率，单位次/秒")
  private Double requestRate;
  @Schema(description = "HTTP 5xx 响应速率，单位次/秒")
  private Double serverErrorRate;
  @Schema(description = "HTTP 请求耗时 P95，单位毫秒；历史聚合取桶内最差一分钟值")
  private Double p95Ms;
  @Schema(description = "HTTP 请求耗时 P99，单位毫秒；历史聚合取桶内最差一分钟值")
  private Double p99Ms;
  @Schema(description = "JVM 线程数")
  private Double threadCount;
  @Schema(description = "JVM 阻塞线程数")
  private Double blockedThreadCount;
  @Schema(description = "数据库连接池使用率，原始比值 0～1；不可用时为 null")
  private Double dbPoolUsage;
  @Schema(description = "数据库连接池等待线程数")
  private Double dbWaiting;
}
