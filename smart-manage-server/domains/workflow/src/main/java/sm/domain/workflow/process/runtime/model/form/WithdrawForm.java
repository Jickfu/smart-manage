package sm.domain.workflow.process.runtime.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
@Schema(description = "申请人撤回审批轮次请求")
public record WithdrawForm(
        @Schema(description = "ID") @NotNull Long id,
        @Schema(description = "客户端请求 UUID，用于同一命令安全重试") @NotNull UUID requestId) { }
