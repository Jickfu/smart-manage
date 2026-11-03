package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "当前用户邮箱验证码申请输入表单")
@Data
public class CurrentUserEmailCodeRequestForm {
    @Schema(description = "密码，按浏览器密码加密协议提交")
    @NotBlank(message = "密码不能为空")
    private String password;
    @Schema(description = "邮箱地址")
    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
