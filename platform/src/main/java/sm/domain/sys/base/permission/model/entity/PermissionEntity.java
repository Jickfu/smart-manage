package sm.domain.sys.base.permission.model.entity;

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
@TableName("t_sys_permission")
public class PermissionEntity extends BaseEntity {
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 编码
	 */
	private String number;
	/**
	 * 应用ID
	 */
	private Long featureId;
	private Long appId;

	@Version
	private Integer version;
}
