package sm.domain.demo.office.leave.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
@Schema(description = "删除未提交的请假单请求")
public record LeaveDeleteForm(
        @Schema(description = "ID") @NotNull Long id,
        @Schema(description = "乐观锁版本，修改已有记录时使用") @NotNull Integer version) { }
