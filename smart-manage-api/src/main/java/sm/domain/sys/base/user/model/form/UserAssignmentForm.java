package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 用户任职明细。 */
@Data
public class UserAssignmentForm {
    @NotNull(message = "部门不能为空")
    private Long orgId;

    @NotBlank(message = "职位不能为空")
    private String position;

    private Boolean isOrgLeader = false;
    private Boolean isPrimary = false;
}
