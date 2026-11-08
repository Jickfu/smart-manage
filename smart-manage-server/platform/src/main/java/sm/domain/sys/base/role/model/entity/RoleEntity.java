package sm.domain.sys.base.role.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 角色持久化实体。
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_role")
public class RoleEntity extends BaseEntity {
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
	 * 说明
	 */
	private String description;
	/**
	 * 默认数据权限范围，取值见 DataScopeType：ALL、ORG_AND_CHILDREN、ORG、SELF、CUSTOM_ORGS
	 */
	private String defaultDataScope;

	/**
	 * 乐观锁版本号
	 */
	@Version
	private Integer version;
}
