package sm.domain.sys.base.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 系统管理-领域（映射 t_sys_domain）
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_domain")
public class DomainEntity extends BaseEntity {
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
	 * 排序号
	 */
	private Integer seq;

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
