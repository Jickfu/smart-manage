package sm.domain.sys.scheduler.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 调度目录的领域与应用投影；执行记录使用执行时快照。
 */
@Schema(description = "调度目录行输出模型")
@Data
public class SchedulerCatalogRowVO {
    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "所属领域名称")
    private String domainName;
    @Schema(description = "所属应用ID")
    private Long appId;
    @Schema(description = "所属应用名称")
    private String appName;
}
