package sm.domain.sys.base.user.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 用户全部任职组织的角色分配保存命令。 */
@Data
public class UserRoleAssignmentSaveForm {
	@NotNull(message = "用户ID不能为空")
	private Long userId;

	@Valid
	@NotNull(message = "组织角色列表不能为空")
	private List<UserOrganizationRoleForm> assignments;
}
