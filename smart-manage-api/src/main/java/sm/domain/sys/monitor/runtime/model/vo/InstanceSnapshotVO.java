package sm.domain.sys.monitor.runtime.model.vo;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** 应用实例遥测，不包含主机文件系统和物理内存。 */
@Data
public class InstanceSnapshotVO {
  private String instanceId;
  private String hostId;
  private Instant sampleTime;
  private RuntimeInfo runtime;
  private CpuInfo cpu;
  private MemoryInfo memory;
  private ThreadInfo threads;
  private List<GcInfo> gc;
  private DataSourceInfo dataSource;
  private HttpInfo http;
  private HealthInfo health;

  @Data
  public static class RuntimeInfo {
    private boolean collectorAvailable;
    private String javaVersion;
    private String javaVendor;
    private String vmName;
    private Instant startTime;
    private long uptimeMs;
    private int processors;
  }

  @Data
  public static class CpuInfo {
    private Double processUsage;
  }

  @Data
  public static class MemoryInfo {
    private boolean collectorAvailable;
    private long heapUsed;
    private long heapCommitted;
    private long heapMax;
    private long nonHeapUsed;
    private long nonHeapCommitted;
  }

  @Data
  public static class ThreadInfo {
    private boolean collectorAvailable;
    private int live;
    private int daemon;
    private int peak;
    private Map<String, Integer> stateCounts;
  }

  @Data
  public static class GcInfo {
    private String name;
    private long collectionCount;
    private long collectionTimeMs;
  }

  @Data
  public static class DataSourceInfo {
    private boolean collectorAvailable;
    private int active;
    private int idle;
    private int maxActive;
    private int waiting;
    private long connectCount;
    private long errorCount;
  }

  @Data
  public static class HttpInfo {
    private Double requestRate;
    private Double clientErrorRate;
    private Double serverErrorRate;
    private Double p95Ms;
    private Double p99Ms;
  }

  @Data
  public static class HealthInfo {
    private boolean collectorAvailable;
    private String status;
    private List<HealthComponent> components;
  }

  @Data
  public static class HealthComponent {
    private String name;
    private String status;
  }
}
