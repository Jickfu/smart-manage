package sm.domain.sys.base.role.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_role")
public class RoleEntity extends BaseEntity {
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	/*
	 * 角色名称
	 */
	private String name;
	/*
	 * 角色编码
	 */
	private String number;
	/*
	 * 角色描述
	 */
	private String description;
	private String defaultDataScope;

	@Version
	private Integer version;
}
