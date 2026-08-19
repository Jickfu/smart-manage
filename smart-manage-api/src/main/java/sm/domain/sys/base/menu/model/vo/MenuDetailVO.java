package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.menu.model.enums.ExternalOpenModeEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;

import java.time.LocalDateTime;

/**
 * 菜单详情 VO
 *
 * @author Chekfu
 */
@Data
@Schema(description = "菜单详情")
public class MenuDetailVO {

	@Schema(description = "ID")
	private Long id;

	@Schema(description = "乐观锁版本")
	private Integer version;

	@Schema(description = "编码")
	private String number;

	@Schema(description = "名称")
	private String name;

	@Schema(description = "层级")
	private MenuLevelEnum level;

	@Schema(description = "所属应用信息")
	private ReferenceInfo app;

	@Schema(description = "所属功能信息")
	private ReferenceInfo feature;

	@Schema(description = "权限信息")
	private ReferenceInfo permission;

	@Schema(description = "路径")
	private String path;

	@Schema(description = "组件")
	private String component;

	@Schema(description = "页面目标类型")
	private MenuTargetTypeEnum targetType;

	@Schema(description = "外部链接地址")
	private String externalUrl;

	@Schema(description = "外部链接打开方式")
	private ExternalOpenModeEnum externalOpenMode;

	@Schema(description = "图标")
	private String icon;

	@Schema(description = "描述")
	private String description;

	@Schema(description = "排序")
	private Integer sort;

	@Schema(description = "是否启用")
	private Boolean enabled;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	@Schema(description = "创建人")
	private Long createUser;

	@Schema(description = "修改人")
	private Long updateUser;

	@Schema(description = "父菜单信息")
	private ReferenceInfo parent;

	@Data
	@Schema(description = "引用对象简要信息")
	public static class ReferenceInfo {
		@Schema(description = "ID")
        private Long id;

		@Schema(description = "编码")
		private String number;

		@Schema(description = "名称")
		private String name;
	}
}
