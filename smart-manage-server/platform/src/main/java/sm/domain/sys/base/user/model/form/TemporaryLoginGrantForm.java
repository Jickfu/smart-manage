package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 一次性代登录凭证生成入参。
 */
@Schema(description = "临时代登录授权输入表单")
@Data
public class TemporaryLoginGrantForm {
    @Schema(description = "用户ID")
    @NotNull(message = "目标用户不能为空")
    private Long userId;

    @Schema(description = "申请临时代登录的原因，供审计追溯")
    @NotBlank(message = "代登录原因不能为空")
    @Size(max = 500, message = "代登录原因不能超过500个字符")
    private String reason;
}
