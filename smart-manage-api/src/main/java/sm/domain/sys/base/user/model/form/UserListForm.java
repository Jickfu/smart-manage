package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * 用户列表查询表单
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户列表表单")
public class UserListForm extends PageForm {
	@Schema(description = "关键词（用户名、昵称模糊匹配）")
	private String keyword;

	@Schema(description = "组织ID")
	private Long orgId;

	@Schema(description = "是否包含下级组织")
	private Boolean includeDescendants;

	@Schema(description = "是否只查询未分配部门的用户")
	private Boolean unassigned;
}
