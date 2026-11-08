package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员代登录功能的二级认证入参。
 */
@Schema(description = "临时代登录安全验证输入表单")
@Data
public class TemporaryLoginSafeForm {
    @Schema(description = "密码，按浏览器密码加密协议提交")
    @NotBlank(message = "管理员密码不能为空")
    private String password;
}
