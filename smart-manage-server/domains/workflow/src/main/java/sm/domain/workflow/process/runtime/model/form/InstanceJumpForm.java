package sm.domain.workflow.process.runtime.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "调整流程节点请求")
public record InstanceJumpForm(
        @Schema(description = "工作流实例 ID") @NotNull Long instanceId,
        @Schema(description = "引擎任务 ID") @NotNull Long taskId,
        @Schema(description = "调整后的目标节点编码") @NotBlank @Size(max = 100) String targetNodeCode,
        @Schema(description = "节点调整方向，取值 RETURN 或 JUMP") @NotBlank @Pattern(regexp = "RETURN|JUMP") String action,
        @Schema(description = "操作原因") @NotBlank @Size(max = 500) String reason,
        @Schema(description = "客户端请求 UUID，用于同一命令安全重试") @NotNull UUID requestId) { }
