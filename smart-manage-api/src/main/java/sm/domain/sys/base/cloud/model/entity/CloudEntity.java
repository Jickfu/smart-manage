package sm.domain.sys.base.cloud.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 系统管理-云（映射 t_sys_cloud）
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_cloud")
public class CloudEntity extends BaseEntity {
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private String name;

	private String number;

	private Integer seq;

	private Boolean enabled;

	@Version
	private Integer version;
}
