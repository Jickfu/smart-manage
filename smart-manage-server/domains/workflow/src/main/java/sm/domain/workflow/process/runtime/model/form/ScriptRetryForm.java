package sm.domain.workflow.process.runtime.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "重试失败脚本节点请求")
public record ScriptRetryForm(
        @Schema(description = "工作流实例 ID") @NotNull Long instanceId,
        @Schema(description = "引擎任务 ID") @NotNull Long taskId,
        @Schema(description = "操作原因") @NotBlank @Size(max = 500) String reason,
        @Schema(description = "客户端请求 UUID，用于同一命令安全重试") @NotNull UUID requestId) { }
