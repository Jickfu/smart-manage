package sm.domain.sys.base.user.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 一次性代登录凭证生成入参。 */
@Data
public class TemporaryLoginGrantForm {
    @NotNull(message = "目标用户不能为空")
    private Long userId;

    @NotBlank(message = "代登录原因不能为空")
    @Size(max = 500, message = "代登录原因不能超过500个字符")
    private String reason;
}
