package sm.domain.sys.monitor.node.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 当前应用实例的生产安全运行快照。 */
@Data
@Schema(description = "当前应用实例运行快照")
public class NodeInfoVO {
    private String instanceId;
    private String sampleTime;
    private RuntimeInfo runtime;
    private OsInfo os;
    private CpuInfo cpu;
    private MemoryInfo memory;
    private DiskInfo disk;
    private ThreadInfo threads;
    private List<GcInfo> gc;
    private DataSourceInfo dataSource;
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
    }

    @Data
    public static class DiskInfo {
        private String mount;
        private long total;
        private long used;
        private long available;
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
