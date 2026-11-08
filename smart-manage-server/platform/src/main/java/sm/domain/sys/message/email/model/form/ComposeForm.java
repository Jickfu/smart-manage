package sm.domain.sys.message.email.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "邮件编写输入表单")
public record ComposeForm(
    @Schema(description = "发信账号 ID，未指定时使用默认发信账号")
    Long accountId,
    @Schema(description = "收件用户 ID 列表")
    @NotEmpty @Size(max = 50) List<@NotNull Long> toUserIds,
    @Schema(description = "抄送用户 ID 列表")
    @Size(max = 50) List<@NotNull Long> ccUserIds,
    @Schema(description = "密送用户 ID 列表")
    @Size(max = 50) List<@NotNull Long> bccUserIds,
    @Schema(description = "主题")
    @NotBlank @Size(max = 300) String subject,
    @Schema(description = "HTML 格式邮件正文")
    @NotBlank @Size(max = 200000) String htmlBody,
    @Schema(description = "纯文本邮件正文")
    @Size(max = 200000) String textBody) {
}
