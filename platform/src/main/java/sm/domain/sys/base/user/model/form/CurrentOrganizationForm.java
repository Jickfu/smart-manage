package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 当前用户切换组织表单。 */
@Data
public class CurrentOrganizationForm {
    @NotNull(message = "组织不能为空")
    private Long orgId;
}
