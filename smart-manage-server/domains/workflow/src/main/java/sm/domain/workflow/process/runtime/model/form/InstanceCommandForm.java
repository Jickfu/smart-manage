package sm.domain.workflow.process.runtime.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "流程实例操作请求")
public record InstanceCommandForm(
        @Schema(description = "工作流实例 ID") @NotNull Long instanceId,
        @Schema(description = "操作原因") @NotBlank @Size(max = 500) String reason,
        @Schema(description = "客户端请求 UUID，用于同一命令安全重试") @NotNull UUID requestId) { }
