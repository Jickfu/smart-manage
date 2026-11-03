package sm.domain.sys.monitor.loginlog.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "登录日志列表项")
public class LoginLogListVO implements Serializable {
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "用户ID")
	private Long userId;
	@Schema(description = "用户名")
	private String username;
	@Schema(description = "昵称")
	private String nickname;
	@Schema(description = "登录认证事件类型，见 LoginEventType")
	private String eventType;
	@Schema(description = "是否成功")
	private Boolean success;
	@Schema(description = "失败原因")
	private String failReason;
	@Schema(description = "IP地址")
	private String ip;
	@Schema(description = "链路追踪ID")
	private String traceId;
	@Schema(description = "创建时间")
	private LocalDateTime createTime;
	@Schema(description = "代登录凭证签发管理员ID")
	private Long issuerUserId;
	@Schema(description = "代登录授权编号")
	private String grantId;
}
