package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 用户在一个任职组织下的完整角色分配结果。 */
@Data
public class UserOrganizationRoleForm {
	@NotNull(message = "组织ID不能为空")
	private Long orgId;

	@NotNull(message = "角色列表不能为空")
	private List<Long> roleIds;
}
