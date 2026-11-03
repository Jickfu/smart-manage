package sm.domain.sys.base.login.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 校验一次登录滑块挑战。
 */
@Schema(description = "验证码校验输入表单")
@Data
public class CaptchaVerifyForm {
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 100, message = "用户名长度不能超过100个字符")
    private String username;

    @Schema(description = "验证码挑战标识")
    @NotBlank(message = "验证码挑战ID不能为空")
    private String challengeId;

    @Schema(description = "验证码行为轨迹")
    @Valid
    @NotNull(message = "滑块轨迹不能为空")
    private CaptchaTrackForm track;
}
