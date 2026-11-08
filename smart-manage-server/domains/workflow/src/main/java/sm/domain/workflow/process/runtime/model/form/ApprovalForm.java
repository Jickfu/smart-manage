package sm.domain.workflow.process.runtime.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description = "同意或拒绝审批任务请求")
public record ApprovalForm(
        @Schema(description = "工作流实例 ID") @NotNull Long instanceId,
        @Schema(description = "引擎任务 ID") @NotNull Long taskId,
        @Schema(description = "审批动作，取值 APPROVE 或 REJECT") @NotBlank @Pattern(regexp = "APPROVE|REJECT") String action,
        @Schema(description = "审批意见，拒绝时必填") @Size(max = 1000) String opinion,
        @Schema(description = "客户端请求 UUID，用于同一命令安全重试") @NotNull java.util.UUID requestId) { }
