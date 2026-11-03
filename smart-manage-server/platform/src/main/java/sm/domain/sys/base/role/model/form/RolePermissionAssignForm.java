package sm.domain.sys.base.role.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色权限分配命令。
 */
@Schema(description = "角色权限关联分配输入表单")
@Data
public class RolePermissionAssignForm {
	@Schema(description = "角色ID")
	@NotNull(message = "角色ID不能为空")
	private Long roleId;

	@Schema(description = "权限ID列表")
	@NotNull(message = "权限列表不能为空")
	private List<Long> permissionIds;
}
