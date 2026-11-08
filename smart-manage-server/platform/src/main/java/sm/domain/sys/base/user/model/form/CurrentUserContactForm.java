package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 当前用户通过二级认证修改手机或邮箱。
 */
@Schema(description = "当前用户联系方式输入表单")
@Data
public class CurrentUserContactForm {
    @Schema(description = "联系方式修改的身份验证方式，当前仅支持 PASSWORD")
    @NotBlank(message = "验证方式不能为空")
    @Pattern(regexp = "PASSWORD", message = "当前仅支持密码验证")
    private String verificationMethod;

    @Schema(description = "密码，按浏览器密码加密协议提交")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "联系方式类型：PHONE 手机、EMAIL 邮箱")
    @NotBlank(message = "联系方式类型不能为空")
    @Pattern(regexp = "PHONE|EMAIL", message = "联系方式类型无效")
    private String type;

    @Schema(description = "待设置的新手机号码或邮箱地址，由 type 决定")
    @NotBlank(message = "新联系方式不能为空")
    private String value;
}
