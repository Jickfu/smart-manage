package sm.domain.sys.base.menu.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.system.form.PageForm;

/**
 * 菜单-基础资料选择-分页查询表单。
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单-基础资料选择-分页查询")
public class MenuSelectForm extends PageForm {
	@Schema(description = "关键词（编码、名称模糊匹配）")
	private String keyword;

	@Schema(description = "应用ID（必填，按应用过滤）")
	private Long appId;

	@Schema(description = "菜单层级（可选，过滤指定层级）")
	private MenuLevelEnum level;

	@Schema(description = "排除ID（编辑时排除自身，避免选到自己）")
	private Long excludeId;

	@Schema(description = "是否启用（默认 true）")
	private Boolean enabled = true;
}
