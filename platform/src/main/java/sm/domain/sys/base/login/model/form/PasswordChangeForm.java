package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "首次登录修改密码表单")
public class PasswordChangeForm {
    @NotBlank(message = "改密凭证不能为空")
    @Schema(description = "一次性改密凭证", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ticket;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "SM2 加密后的新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
