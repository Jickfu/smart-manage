package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "邮箱密码重置验证码申请输入表单")
@Data
public class EmailPasswordCodeRequestForm {
    @Schema(description = "邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    @Schema(description = "验证码校验通过后取得的一次性票据")
    @NotBlank(message = "滑块票据不能为空")
    private String captchaTicket;
}
