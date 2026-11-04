package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "当前用户邮箱验证改密输入表单")
@Data
public class CurrentUserEmailPasswordForm {
    @Schema(description = "邮箱验证码")
    @NotBlank(message = "邮箱验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "邮箱验证码必须为6位数字")
    private String code;
    @Schema(description = "新密码，按浏览器密码加密协议提交")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
