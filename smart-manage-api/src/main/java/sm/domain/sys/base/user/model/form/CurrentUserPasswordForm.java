package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 当前用户修改密码表单，密码字段使用 SM2 加密传输。 */
@Data
public class CurrentUserPasswordForm {
    @NotBlank(message = "原密码不能为空")
    private String currentPassword;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
