package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 当前用户原密码校验表单，密码字段使用 SM2 加密传输。
 */
@Schema(description = "当前用户密码验证输入表单")
@Data
public class CurrentUserPasswordVerifyForm {
    @Schema(description = "当前密码，按浏览器密码加密协议提交")
    @NotBlank(message = "原密码不能为空")
    private String currentPassword;
}
