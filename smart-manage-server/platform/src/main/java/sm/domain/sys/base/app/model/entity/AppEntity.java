package sm.domain.sys.base.app.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 系统管理-应用（映射 t_sys_app）
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_app")
public class AppEntity extends BaseEntity {
	/**
	 * ID
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 业务编码
	 */
	private String number;

	/**
	 * 图标
	 */
	private String icon;

	/**
	 * 图标颜色（SVG fill）
	 */
	private String iconColor;

	/**
	 * 排序号
	 */
	private Integer seq;

	/**
	 * 说明
	 */
	private String description;

	/**
	 * 所属领域ID
	 */
	private Long domainId;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

	/**
	 * 乐观锁版本号
	 */
	@Version
	private Integer version;
}
