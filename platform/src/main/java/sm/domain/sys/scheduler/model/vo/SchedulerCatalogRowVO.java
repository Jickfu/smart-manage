package sm.domain.sys.scheduler.model.vo;

import lombok.Data;

/** 调度目录的领域与应用投影；执行记录使用执行时快照。 */
@Data
public class SchedulerCatalogRowVO {
    private Long domainId;
    private String domainName;
    private Long appId;
    private String appName;
}
