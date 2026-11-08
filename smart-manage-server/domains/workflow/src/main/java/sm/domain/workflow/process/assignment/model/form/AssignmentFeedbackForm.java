package sm.domain.workflow.process.assignment.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
@Schema(description = "审批人引用回显请求")
public record AssignmentFeedbackForm(
        @Schema(description = "逗号分隔的审批人规则标识") @Size(max = 10000) String storageIds) { }
