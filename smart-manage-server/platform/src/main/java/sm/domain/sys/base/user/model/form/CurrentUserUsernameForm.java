package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "当前用户修改用户名表单")
public record CurrentUserUsernameForm(
        @Schema(description = "修改后的登录用户名")
        @NotBlank @Size(max = 100) String username,
        @Schema(description = "按浏览器加密协议提交的当前密码")
        @NotBlank @Size(max = 4096) String currentPassword) {
}
