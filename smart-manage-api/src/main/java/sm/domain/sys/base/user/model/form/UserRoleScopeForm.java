package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 查询用户在指定任职组织下的角色关系。 */
@Data
public class UserRoleScopeForm {
	@NotNull(message = "用户ID不能为空")
	private Long userId;

	@NotNull(message = "组织ID不能为空")
	private Long orgId;
}
