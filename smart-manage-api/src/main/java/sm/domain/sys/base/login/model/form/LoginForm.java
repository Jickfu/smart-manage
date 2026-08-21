package sm.domain.sys.base.login.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author Chekfu
 */
@Data
public class LoginForm {
	@NotBlank(message = "用户名不能为空")
	private String username;

	@NotBlank(message = "密码不能为空")
	private String password;

	@NotBlank(message = "滑块验证票据不能为空")
	private String captchaTicket;
}
