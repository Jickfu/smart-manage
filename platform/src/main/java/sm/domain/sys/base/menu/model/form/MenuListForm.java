package sm.domain.sys.base.menu.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单列表表单")
public class MenuListForm extends PageForm {
	@Schema(description = "按应用筛选")
	private Long appId;

	@Schema(description = "按父菜单筛选")
	private Long parentId;

	@Schema(description = "关键词（名称、路径模糊匹配）")
	private String keyword;
}
