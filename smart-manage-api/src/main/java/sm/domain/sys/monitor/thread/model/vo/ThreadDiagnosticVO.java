package sm.domain.sys.monitor.thread.model.vo;

import lombok.Data;

import java.util.List;

/** 指定实例的一次结构化线程诊断快照。 */
@Data
public class ThreadDiagnosticVO {
    private String instanceId;
    private String sampleTime;
    private Integer sampleMillis;
    private List<ThreadItem> threads;

    @Data
    public static class ThreadItem {
        private long id;
        private String name;
        private String state;
        private boolean daemon;
        private int priority;
        private Double cpuUsage;
        private long blockedCount;
        private long waitedCount;
        private String lockName;
        private Long lockOwnerId;
        private String lockOwnerName;
        private boolean deadlocked;
        private List<String> stackTrace;
        private List<String> lockedMonitors;
        private List<String> lockedSynchronizers;
    }
}
