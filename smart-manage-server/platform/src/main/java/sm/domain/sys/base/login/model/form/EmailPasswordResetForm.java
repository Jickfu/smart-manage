package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "邮箱密码重置输入表单")
@Data
public class EmailPasswordResetForm {
    @Schema(description = "邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    @Schema(description = "邮箱验证码")
    @NotBlank(message = "邮箱验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "邮箱验证码必须为6位数字")
    private String code;
    @Schema(description = "新密码，按浏览器密码加密协议提交")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
