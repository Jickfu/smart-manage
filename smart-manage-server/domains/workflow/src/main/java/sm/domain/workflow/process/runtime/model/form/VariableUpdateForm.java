package sm.domain.workflow.process.runtime.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "修改已挂起实例的流程变量请求")
public record VariableUpdateForm(
        @Schema(description = "工作流实例 ID") @NotNull Long instanceId,
        @Schema(description = "待写入的变量映射，键为变量名") @NotNull Map<String, Object> changes,
        @Schema(description = "待删除的变量名列表") @NotNull List<String> removals,
        @Schema(description = "操作原因") @NotBlank @Size(max = 500) String reason,
        @Schema(description = "客户端请求 UUID，用于同一命令安全重试") @NotNull UUID requestId) { }
