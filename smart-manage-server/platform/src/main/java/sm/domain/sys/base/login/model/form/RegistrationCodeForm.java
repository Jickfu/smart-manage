package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "邮箱注册验证码发送表单")
public record RegistrationCodeForm(
        @Schema(description = "接收验证码的邮箱地址")
        @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "滑块验证签发的一次性票据")
        @NotBlank @Size(max = 128) String captchaTicket) {
}
