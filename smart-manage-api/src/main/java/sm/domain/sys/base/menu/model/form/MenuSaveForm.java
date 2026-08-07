package sm.domain.sys.base.menu.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import sm.domain.sys.base.common.enums.MenuLevelEnum;

/**
 * @author Chekfu
 */
@Data
@Schema(description = "菜单保存")
public class MenuSaveForm {
	@Schema(description = "id，为空则新增")
	private Long id;

	@Schema(description = "乐观锁版本，编辑时必填")
	private Integer version;

	@NotBlank(message = "编码不能为空")
	@Pattern(regexp = "^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$", message = "编码必须为小写字母、数字和下划线，且以小写字母开头")
	@Schema(description = "编码：小写字母、数字和下划线组成，以下划线分隔单词")
	private String number;

	@NotBlank(message = "名称不能为空")
	@Schema(description = "名称")
	private String name;

	@NotNull(message = "类型不能为空")
	@Schema(description = "菜单层级")
	private MenuLevelEnum level;

	@Schema(description = "父级 id")
	private Long parentId;

	@NotNull(message = "应用不能为空")
	@Schema(description = "应用 id")
	private Long appId;

	@Schema(description = "权限 id，页面层级必填")
	private Long permissionId;

	@Schema(description = "路径")
	private String path;

	@Schema(description = "组件")
	private String component;

	@Schema(description = "图标")
	private String icon;

	@Schema(description = "描述")
	private String description;

	@Schema(description = "排序")
	private Integer sort;

}
