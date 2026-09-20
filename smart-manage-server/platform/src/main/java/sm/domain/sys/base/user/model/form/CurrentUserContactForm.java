package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 当前用户通过二级认证修改手机或邮箱。 */
@Data
public class CurrentUserContactForm {
    @NotBlank(message = "验证方式不能为空")
    @Pattern(regexp = "PASSWORD", message = "当前仅支持密码验证")
    private String verificationMethod;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "联系方式类型不能为空")
    @Pattern(regexp = "PHONE|EMAIL", message = "联系方式类型无效")
    private String type;

    @NotBlank(message = "新联系方式不能为空")
    private String value;
}
