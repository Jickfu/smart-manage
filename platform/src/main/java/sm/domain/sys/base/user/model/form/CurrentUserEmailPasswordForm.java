package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CurrentUserEmailPasswordForm {
    @NotBlank(message = "邮箱验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "邮箱验证码必须为6位数字")
    private String code;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
