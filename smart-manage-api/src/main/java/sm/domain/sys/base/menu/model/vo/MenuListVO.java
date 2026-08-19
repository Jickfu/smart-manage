package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.menu.model.enums.ExternalOpenModeEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;

/**
 * @author Chekfu
 */
@Data
@Schema(title = "菜单列表视图")
public class MenuListVO {
	@Schema(description = "id")
	private Long id;

	@Schema(description = "编号")
	private String number;

	@Schema(description = "菜单层级")
	private MenuLevelEnum level;

	@Schema(description = "父菜单ID，一级菜单为0")
	private Long parentId;

	@Schema(description = "菜单名称")
	private String name;

	@Schema(description = "菜单路径")
	private String path;

	@Schema(description = "组件路径")
	private String component;

	@Schema(description = "页面目标类型")
	private MenuTargetTypeEnum targetType;

	@Schema(description = "外部链接地址")
	private String externalUrl;

	@Schema(description = "外部链接打开方式")
	private ExternalOpenModeEnum externalOpenMode;

	@Schema(description = "菜单排序")
	private Integer sort;

	@Schema(description = "图标")
	private String icon;

	@Schema(description = "启用状态")
	private Boolean enabled;
}
