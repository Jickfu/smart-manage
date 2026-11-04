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
	/**
	 * ID
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	/**
	 * 用户ID
	 */
	private Long userId;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 登录认证事件类型，见 LoginEventType
	 */
	private String eventType;
	/**
	 * 是否成功
	 */
	private Boolean success;
	/**
	 * 失败原因
	 */
	private String failReason;
	/**
	 * IP地址
	 */
	private String ip;
	/**
	 * 客户端 User-Agent
	 */
	private String userAgent;
	/**
	 * 链路追踪ID
	 */
	private String traceId;
	/**
	 * 代登录凭证签发管理员ID
	 */
	private Long issuerUserId;
	/**
	 * 代登录授权编号
	 */
	private String grantId;
	/**
	 * 代登录原因
	 */
	private String grantReason;
	/**
	 * 代登录凭证计划失效时间
	 */
	private java.time.LocalDateTime grantExpiresAt;
}
