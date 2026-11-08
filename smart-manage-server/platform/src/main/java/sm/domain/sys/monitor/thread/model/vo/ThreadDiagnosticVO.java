package sm.domain.sys.monitor.thread.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

/**
 * 指定实例的一次结构化线程诊断快照。
 */
@Schema(description = "线程诊断输出模型")
@Data
public class ThreadDiagnosticVO {
    @Schema(description = "应用实例唯一标识")
    private String instanceId;
    @Schema(description = "采样时间")
    private String sampleTime;
    @Schema(description = "线程 CPU 采样间隔，单位毫秒")
    private Integer sampleMillis;
    @Schema(description = "线程信息")
    private List<ThreadItem> threads;

    @Schema(description = "线程诊断条目")
    @Data
    public static class ThreadItem {
        @Schema(description = "JVM 线程 ID")
        private long id;
        @Schema(description = "线程名称")
        private String name;
        @Schema(description = "Java Thread.State 线程状态名称")
        private String state;
        @Schema(description = "是否为守护线程")
        private boolean daemon;
        @Schema(description = "线程优先级")
        private int priority;
        @Schema(description = "线程在采样间隔内的 CPU 使用率，单位百分比，取值 0～100；未采样时为 null")
        private Double cpuUsage;
        @Schema(description = "线程累计阻塞次数")
        private long blockedCount;
        @Schema(description = "线程累计等待次数")
        private long waitedCount;
        @Schema(description = "线程正在等待或持有的锁名称")
        private String lockName;
        @Schema(description = "锁持有线程 ID")
        private Long lockOwnerId;
        @Schema(description = "锁持有线程名称")
        private String lockOwnerName;
        @Schema(description = "是否为死锁线程")
        private boolean deadlocked;
        @Schema(description = "线程栈信息，仅供管理员线程诊断")
        private List<String> stackTrace;
        @Schema(description = "线程持有的监视器锁信息")
        private List<String> lockedMonitors;
        @Schema(description = "线程持有的同步器锁信息")
        private List<String> lockedSynchronizers;
    }
}
