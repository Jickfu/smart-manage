package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.menu.model.enums.ExternalOpenModeEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;

import java.util.List;

/**
 * @author Chekfu
 */
@Data
public class MenuVO {
	@Schema(description = "菜单ID")
	private Long id;

	@Schema(description = "名称")
	private String name;

	@Schema(description = "路由地址")
	private String path;

	@Schema(description = "组件标识（前端白名单注册表解析）")
	private String component;

	@Schema(description = "页面目标类型")
	private MenuTargetTypeEnum targetType;

	@Schema(description = "外部链接地址")
	private String externalUrl;

	@Schema(description = "外部链接打开方式")
	private ExternalOpenModeEnum externalOpenMode;

	@Schema(description = "图标")
	private String icon;

	@Schema(description = "菜单等级")
	private MenuLevelEnum level;

	@Schema(description = "子菜单")
	private List<MenuVO> routes;

}

