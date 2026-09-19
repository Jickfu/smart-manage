package sm.domain.sys.scheduler.model.vo;

import lombok.Data;

/** 执行开始时一次联表读取的任务及业务归属快照。 */
@Data
public class JobExecutionSnapshotVO {
    private Long jobId;
    private String jobName;
    private Long domainId;
    private String domainName;
    private Long appId;
    private String appName;
}
