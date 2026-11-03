package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Chekfu
 */
@Schema(description = "登录输入表单")
@Data
public class LoginForm {
	@Schema(description = "用户名")
	@NotBlank(message = "用户名不能为空")
	private String username;

	@Schema(description = "密码，按浏览器密码加密协议提交")
	@NotBlank(message = "密码不能为空")
	private String password;

	@Schema(description = "验证码校验通过后取得的一次性票据")
	@NotBlank(message = "滑块验证票据不能为空")
	private String captchaTicket;
}
