package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 用户在一个任职组织下的完整角色分配结果。
 */
@Schema(description = "用户组织角色输入表单")
@Data
public class UserOrganizationRoleForm {
	@Schema(description = "组织 ID")
	@NotNull(message = "组织ID不能为空")
	private Long orgId;

	@Schema(description = "当前组织下的角色ID列表")
	@NotNull(message = "角色列表不能为空")
	private List<Long> roleIds;
}
