package sm.domain.workflow.process.runtime.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "审批任务协作请求")
public record TaskCooperationForm(
        @Schema(description = "工作流实例 ID") @NotNull Long instanceId,
        @Schema(description = "引擎任务 ID") @NotNull Long taskId,
        @Schema(description = "协作动作，取值 TRANSFER、DELEGATE、ADD_SIGN 或 REDUCE_SIGN") @NotBlank @Pattern(regexp = "TRANSFER|DELEGATE|ADD_SIGN|REDUCE_SIGN") String action,
        @Schema(description = "参与任务协作的目标用户 ID 列表") @NotEmpty @Size(max = 100) List<@NotNull Long> targetUserIds,
        @Schema(description = "操作原因") @NotBlank @Size(max = 500) String reason,
        @Schema(description = "客户端请求 UUID，用于同一命令安全重试") @NotNull UUID requestId) { }
