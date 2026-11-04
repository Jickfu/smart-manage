package sm.domain.sys.message.email.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "发信账号测试输入表单")
public record AccountTestForm(
    @Schema(description = "发信账号 ID")
    @NotNull Long accountId,
    @Schema(description = "连接测试邮件的收件邮箱")
    @Email @Size(max = 320) String recipient) {
}
