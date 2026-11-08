package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建登录滑块挑战。
 */
@Schema(description = "验证码挑战输入表单")
@Data
public class CaptchaChallengeForm {
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 255, message = "登录标识长度不能超过255个字符")
    private String username;
}
