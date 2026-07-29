package sm.domain.sys.base.login.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chekfu
 */
@Data
@Schema(title = "登录返回视图")
public class LoginVO {

	@Schema(description = "token")
	private String token;

	@Schema(description = "姓名")
	private String nickname;

	@Schema(description = "消息")
	private String msg;

	@Schema(description = "是否必须修改密码")
	private Boolean passwordReset;

	@Schema(description = "一次性改密凭证")
	private String passwordChangeTicket;

	/**
	 * 与前端 Umi access 插件对齐的全局权限标识（如 kdcloud 表示可访问演示路由）
	 */
	@Schema(description = "前端 access 标识")
	private String access;

	public LoginVO() {
	}

	public LoginVO(String msg) {
		this.msg = msg;
	}
}
