package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.common.enums.MenuLevelEnum;

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

	@Schema(description = "应用ID")
	private Long appId;

	@Schema(description = "权限ID")
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
	private ParentInfo parent;

	@Data
	@Schema(description = "父菜单简要信息")
	public static class ParentInfo {
		@Schema(description = "ID")
        private Long id;

		@Schema(description = "编码")
		private String number;

		@Schema(description = "名称")
		private String name;
	}
}
