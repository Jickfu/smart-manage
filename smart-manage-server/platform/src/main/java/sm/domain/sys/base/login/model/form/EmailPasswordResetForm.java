package sm.domain.sys.base.login.model.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmailPasswordResetForm {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    @NotBlank(message = "邮箱验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "邮箱验证码必须为6位数字")
    private String code;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
