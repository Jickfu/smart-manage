package sm.domain.sys.base.role.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色列表表单")
public class RoleListForm extends PageForm {
	@Schema(description = "关键词（角色名称、编码模糊匹配）")
	private String keyword;
}
