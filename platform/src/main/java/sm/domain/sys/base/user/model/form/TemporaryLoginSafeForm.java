package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 管理员代登录功能的二级认证入参。 */
@Data
public class TemporaryLoginSafeForm {
    @NotBlank(message = "管理员密码不能为空")
    private String password;
}
