package sm.domain.sys.monitor.loginlog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 登录/登出日志
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_login_log")
public class LoginLogEntity extends BaseEntity {
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	private Long userId;
	private String username;
	private String nickname;
	private String eventType;
	private Boolean success;
	private String failReason;
	private String ip;
	private String userAgent;
	private String traceId;
}
