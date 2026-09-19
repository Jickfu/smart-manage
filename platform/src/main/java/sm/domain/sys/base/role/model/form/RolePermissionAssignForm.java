package sm.domain.sys.base.role.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 角色权限分配命令。 */
@Data
public class RolePermissionAssignForm {
	@NotNull(message = "角色ID不能为空")
	private Long roleId;

	@NotNull(message = "权限列表不能为空")
	private List<Long> permissionIds;
}
