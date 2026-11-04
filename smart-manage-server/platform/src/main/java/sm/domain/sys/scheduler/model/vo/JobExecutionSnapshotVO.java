package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 执行开始时一次联表读取的任务及业务归属快照。
 */
@Schema(description = "任务执行快照输出模型")
@Data
public class JobExecutionSnapshotVO {
    @Schema(description = "任务ID")
    private Long jobId;
    @Schema(description = "任务名称")
    private String jobName;
    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "执行时所属领域名称快照")
    private String domainName;
    @Schema(description = "所属应用ID")
    private Long appId;
    @Schema(description = "执行时所属应用名称快照")
    private String appName;
}
