package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行实例详情。
 */
@Data
@Schema(description = "执行实例详情")
public class JobLogDetailVO {

    @Schema(description = "ID")
    private Long id;
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
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "耗时(ms)")
    private Long durationMs;
    @Schema(description = "任务执行状态：RUNNING 执行中、SUCCESS 成功、FAILED 失败")
    private String status;
    @Schema(description = "错误说明")
    private String errorMessage;
    @Schema(description = "链路追踪ID")
    private String traceId;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
