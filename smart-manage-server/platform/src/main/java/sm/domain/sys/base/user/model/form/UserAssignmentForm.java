package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户任职明细。
 */
@Schema(description = "用户任职输入表单")
@Data
public class UserAssignmentForm {
    @Schema(description = "组织 ID")
    @NotNull(message = "部门不能为空")
    private Long orgId;

    @Schema(description = "任职岗位名称")
    @NotBlank(message = "职位不能为空")
    private String position;

    @Schema(description = "是否为部门负责人")
    private Boolean isOrgLeader = false;
    @Schema(description = "是否为主职")
    private Boolean isPrimary = false;
}
