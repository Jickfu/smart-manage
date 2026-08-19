package sm.domain.sys.base.menu.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.menu.model.enums.ExternalOpenModeEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;
import sm.system.entity.BaseEntity;

/**
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_menu")
public class MenuEntity extends BaseEntity {
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	@Version
	private Integer version;
	/**
	 * 编号
	 */
	private String number;
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 类型
	 */
	private MenuLevelEnum level;
	/**
	 * 父菜单ID，一级菜单为0
	 */
	private Long parentId;
	/**
	 * 应用ID
	 */
	private Long appId;
	private Long featureId;
	/**
	 * 权限ID
	 */
	private Long permissionId;
	/**
	 * 路径
	 */
	private String path;
	/**
	 * 组件路径
	 */
	private String component;
	/**
	 * 页面目标类型；分组菜单为空。
	 */
	private MenuTargetTypeEnum targetType;
	/**
	 * 外部链接地址；仅外部链接菜单使用。
	 */
	private String externalUrl;
	/**
	 * 外部链接打开方式；仅外部链接菜单使用。
	 */
	private ExternalOpenModeEnum externalOpenMode;
	/**
	 * 图标
	 */
	private String icon;
	/**
	 * 描述
	 */
	private String description;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 是否启用
	 */
	private Boolean enabled;
}
