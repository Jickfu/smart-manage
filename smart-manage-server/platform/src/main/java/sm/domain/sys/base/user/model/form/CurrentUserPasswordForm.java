package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 当前用户修改密码表单，密码字段使用 SM2 加密传输。
 */
@Schema(description = "当前用户密码修改输入表单")
@Data
public class CurrentUserPasswordForm {
    @Schema(description = "当前密码，按浏览器密码加密协议提交")
    @NotBlank(message = "原密码不能为空")
    private String currentPassword;
    @Schema(description = "新密码，按浏览器密码加密协议提交")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
