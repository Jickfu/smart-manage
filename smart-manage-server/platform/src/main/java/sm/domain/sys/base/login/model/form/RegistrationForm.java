package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "邮箱注册提交表单")
public record RegistrationForm(
        @Schema(description = "已接收验证码的邮箱地址")
        @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "六位注册验证码")
        @NotBlank @Pattern(regexp = "[0-9]{6}") String code,
        @Schema(description = "按浏览器加密协议提交的登录密码")
        @NotBlank @Size(max = 4096) String password) {
}
