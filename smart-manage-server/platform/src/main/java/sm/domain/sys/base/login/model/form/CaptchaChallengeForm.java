package sm.domain.sys.base.login.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建登录滑块挑战。 */
@Data
public class CaptchaChallengeForm {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 100, message = "用户名长度不能超过100个字符")
    private String username;
}
