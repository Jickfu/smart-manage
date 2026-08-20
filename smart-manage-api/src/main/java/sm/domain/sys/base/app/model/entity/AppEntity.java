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
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private String name;

	private String number;

	private String icon;

	private String iconColor;

	private Integer seq;

	private String description;

	private Long domainId;

	private Boolean enabled;

	@Version
	private Integer version;
}
