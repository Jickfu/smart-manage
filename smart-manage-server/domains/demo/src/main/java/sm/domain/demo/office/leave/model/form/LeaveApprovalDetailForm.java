package sm.domain.demo.office.leave.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
@Schema(description = "请假单审批轮次快照查询请求")
public record LeaveApprovalDetailForm(
        @Schema(description = "ID") @NotNull Long id,
        @Schema(description = "工作流实例 ID") @NotNull Long instanceId) { }
