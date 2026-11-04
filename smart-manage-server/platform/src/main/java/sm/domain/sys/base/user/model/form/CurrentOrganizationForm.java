package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 当前用户切换组织表单。
 */
@Schema(description = "当前组织切换输入表单")
@Data
public class CurrentOrganizationForm {
    @Schema(description = "组织 ID")
    @NotNull(message = "组织不能为空")
    private Long orgId;
}
