package sm.domain.sys.base.user.apppin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/** 当前用户固定的应用关系。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user_app_pin")
public class UserAppPinEntity extends BaseEntity {
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	private Long userId;
	private Long appId;
	private Integer seq;
}
