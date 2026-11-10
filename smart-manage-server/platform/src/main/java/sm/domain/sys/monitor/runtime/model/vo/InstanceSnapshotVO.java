package sm.domain.sys.monitor.runtime.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import sm.system.time.BusinessTime;

/**
 * 应用实例遥测，不包含主机文件系统和物理内存。
 */
@Schema(description = "应用实例遥测快照输出模型")
@Data
public class InstanceSnapshotVO {
  @Schema(description = "应用实例唯一标识")
  private String instanceId;
  @Schema(description = "运行主机稳定标识")
  private String hostId;
  @Schema(description = "采样时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = BusinessTime.ZONE_NAME)
  private Instant sampleTime;
  @Schema(description = "JVM 运行时信息")
  private RuntimeInfo runtime;
  @Schema(description = "CPU 指标快照")
  private CpuInfo cpu;
  @Schema(description = "内存指标快照")
  private MemoryInfo memory;
  @Schema(description = "线程信息")
  private ThreadInfo threads;
  @Schema(description = "垃圾回收器统计列表")
  private List<GcInfo> gc;
  @Schema(description = "数据库连接池摘要")
  private DataSourceInfo dataSource;
  @Schema(description = "HTTP 请求统计摘要")
  private HttpInfo http;
  @Schema(description = "健康状态摘要")
  private HealthInfo health;

  @Schema(description = "JVM 运行时信息")
  @Data
  public static class RuntimeInfo {
    @Schema(description = "该采集器本次是否可用；为 false 时其数值不能解释为有效零值")
    private boolean collectorAvailable;
    @Schema(description = "Java 运行时版本")
    private String javaVersion;
    @Schema(description = "JVM 供应商")
    private String javaVendor;
    @Schema(description = "JVM 名称")
    private String vmName;
    @Schema(description = "JVM 启动时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = BusinessTime.ZONE_NAME)
    private Instant startTime;
    @Schema(description = "运行时长，单位毫秒")
    private long uptimeMs;
    @Schema(description = "JVM 可用处理器数量")
    private int processors;
  }

  @Schema(description = "CPU 指标")
  @Data
  public static class CpuInfo {
    @Schema(description = "JVM 进程 CPU 使用率，原始比值 0～1；不可用时为 null")
    private Double processUsage;
  }

  @Schema(description = "内存指标")
  @Data
  public static class MemoryInfo {
    @Schema(description = "该采集器本次是否可用；为 false 时其数值不能解释为有效零值")
    private boolean collectorAvailable;
    @Schema(description = "JVM 堆内存已用字节数")
    private long heapUsed;
    @Schema(description = "JVM 已提交堆内存，单位字节")
    private long heapCommitted;
    @Schema(description = "JVM 最大堆内存，单位字节；未定义时为 -1，仅在 collectorAvailable 为 true 时有效")
    private long heapMax;
    @Schema(description = "JVM 已使用非堆内存，单位字节")
    private long nonHeapUsed;
    @Schema(description = "JVM 已提交非堆内存，单位字节")
    private long nonHeapCommitted;
  }

  @Schema(description = "线程统计")
  @Data
  public static class ThreadInfo {
    @Schema(description = "该采集器本次是否可用；为 false 时其数值不能解释为有效零值")
    private boolean collectorAvailable;
    @Schema(description = "当前存活线程数量")
    private int live;
    @Schema(description = "守护线程数量")
    private int daemon;
    @Schema(description = "JVM 启动以来的线程峰值")
    private int peak;
    @Schema(description = "各线程状态的数量，键为 Java Thread.State 名称")
    private Map<String, Integer> stateCounts;
  }

  @Schema(description = "垃圾回收器统计")
  @Data
  public static class GcInfo {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "垃圾回收累计次数，JVM 不支持时可能为负值")
    private long collectionCount;
    @Schema(description = "垃圾回收累计耗时，单位毫秒；JVM 不支持时可能为负值")
    private long collectionTimeMs;
  }

  @Schema(description = "连接池摘要")
  @Data
  public static class DataSourceInfo {
    @Schema(description = "该采集器本次是否可用；为 false 时其数值不能解释为有效零值")
    private boolean collectorAvailable;
    @Schema(description = "当前活动连接数")
    private int active;
    @Schema(description = "当前空闲连接数")
    private int idle;
    @Schema(description = "连接池最大活动连接数")
    private int maxActive;
    @Schema(description = "当前等待获取连接的线程数")
    private int waiting;
    @Schema(description = "连接池累计建立连接数")
    private long connectCount;
    @Schema(description = "累计错误次数")
    private long errorCount;
  }

  @Schema(description = "HTTP 请求统计")
  @Data
  public static class HttpInfo {
    @Schema(description = "相邻采样之间的 HTTP 请求平均速率，单位次/秒；首次采样或不可用时为 null")
    private Double requestRate;
    @Schema(description = "相邻采样之间的 HTTP 4xx 平均速率，单位次/秒；首次采样或不可用时为 null")
    private Double clientErrorRate;
    @Schema(description = "相邻采样之间的 HTTP 5xx 平均速率，单位次/秒；首次采样或不可用时为 null")
    private Double serverErrorRate;
    @Schema(description = "当前实例总体 HTTP 请求耗时 P95，单位毫秒；来源为 Micrometer Timer 分布快照，不可用时为 null")
    private Double p95Ms;
    @Schema(description = "当前实例总体 HTTP 请求耗时 P99，单位毫秒；来源为 Micrometer Timer 分布快照，不可用时为 null")
    private Double p99Ms;
  }

  @Schema(description = "健康摘要")
  @Data
  public static class HealthInfo {
    @Schema(description = "该采集器本次是否可用；为 false 时其数值不能解释为有效零值")
    private boolean collectorAvailable;
    @Schema(description = "健康状态码，如 UP、DOWN、OUT_OF_SERVICE、UNKNOWN；采集失败为 UNKNOWN")
    private String status;
    @Schema(description = "健康组件名称及状态列表，不包含健康详情")
    private List<HealthComponent> components;
  }

  @Schema(description = "健康组件摘要")
  @Data
  public static class HealthComponent {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "组件健康状态码，如 UP、DOWN、OUT_OF_SERVICE、UNKNOWN")
    private String status;
  }
}
