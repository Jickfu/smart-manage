package sm.domain.demo.office.leave.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.UUID;
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "提交请假单并发起审批请求")
public class LeaveSubmitForm extends LeaveSaveForm {
    @Schema(description = "客户端请求 UUID，用于同一命令安全重试")
    @NotNull private UUID requestId;
}
