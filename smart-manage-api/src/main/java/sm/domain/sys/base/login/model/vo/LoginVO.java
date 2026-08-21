package sm.domain.sys.base.login.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chekfu
 */
@Data
@Schema(title = "登录返回视图")
public class LoginVO {

	@Schema(description = "是否已建立登录会话")
	private Boolean authenticated;

	@Schema(description = "消息")
	private String msg;

	@Schema(description = "是否必须修改密码")
	private Boolean passwordReset;

	@Schema(description = "一次性改密凭证")
	private String passwordChangeTicket;

	public LoginVO() {
	}

	public LoginVO(String msg) {
		this.msg = msg;
	}
}
