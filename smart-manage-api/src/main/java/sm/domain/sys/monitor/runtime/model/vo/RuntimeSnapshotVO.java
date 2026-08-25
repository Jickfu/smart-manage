package sm.domain.sys.monitor.runtime.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 当前应用实例及其运行主机的生产安全快照。 */
@Data
@Schema(description = "当前应用实例运行快照")
public class RuntimeSnapshotVO {
    private String instanceId;
    private String hostId;
    private String sampleTime;
    private RuntimeInfo runtime;
    private OsInfo os;
    private CpuInfo cpu;
    private MemoryInfo memory;
    private List<FilesystemInfo> filesystems;
    private IoInfo io;
    private ThreadInfo threads;
    private List<GcInfo> gc;
    private DataSourceInfo dataSource;
    private HttpInfo http;
    private HealthInfo health;

    @Data
    public static class RuntimeInfo {
        private String javaVersion;
        private String javaVendor;
        private String vmName;
        private String startTime;
        private long uptimeMs;
        private int processors;
    }

    @Data
    public static class OsInfo {
        private String name;
        private String version;
        private String arch;
    }

    @Data
    public static class CpuInfo {
        private Double systemUsage;
        private Double processUsage;
        private Double loadAverage;
    }

    @Data
    public static class MemoryInfo {
        private long heapUsed;
        private long heapCommitted;
        private long heapMax;
        private long nonHeapUsed;
        private long nonHeapCommitted;
        private long physicalTotal;
        private long physicalAvailable;
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
    }

    @Data
    public static class IoInfo {
        private long diskReadBytes;
        private long diskWriteBytes;
        private Double diskReadBytesPerSecond;
        private Double diskWriteBytesPerSecond;
        private long networkReceiveBytes;
        private long networkTransmitBytes;
        private Double networkReceiveBytesPerSecond;
        private Double networkTransmitBytesPerSecond;
    }

    @Data
    public static class ThreadInfo {
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
        private String status;
        private List<HealthComponent> components;
    }

    @Data
    public static class HealthComponent {
        private String name;
        private String status;
    }
}
