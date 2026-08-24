package sm.domain.sys.message.email.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ComposeForm(
        Long accountId,
        @NotEmpty @Size(max = 50) List<@NotNull Long> toUserIds,
        @Size(max = 50) List<@NotNull Long> ccUserIds,
        @Size(max = 50) List<@NotNull Long> bccUserIds,
        @NotBlank @Size(max = 300) String subject,
        @NotBlank @Size(max = 200000) String htmlBody,
        @Size(max = 200000) String textBody
) {
}
