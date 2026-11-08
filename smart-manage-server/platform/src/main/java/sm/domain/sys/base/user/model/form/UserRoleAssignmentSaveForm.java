package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用户全部任职组织的角色分配保存命令。
 */
@Schema(description = "用户角色分配保存输入表单")
@Data
public class UserRoleAssignmentSaveForm {
	@Schema(description = "用户ID")
	@NotNull(message = "用户ID不能为空")
	private Long userId;

	@Schema(description = "用户任职明细")
	@Valid
	@NotNull(message = "组织角色列表不能为空")
	private List<UserOrganizationRoleForm> assignments;
}
