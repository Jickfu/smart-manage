package sm.domain.sys.message.email.model.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountSaveForm(
        Long id,
        Integer version,
        @NotBlank @Size(max = 64) String number,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String host,
        @NotNull @Min(1) @Max(65535) Integer port,
        @NotBlank String securityMode,
        @NotBlank @Size(max = 255) String username,
        @Size(max = 1000) String password,
        @NotBlank @Email @Size(max = 320) String fromAddress,
        @Size(max = 100) String fromName,
        @Email @Size(max = 320) String replyTo,
        @NotNull Boolean defaultAccount,
        @NotNull Boolean allowManual,
        @NotNull @Min(1000) @Max(60000) Integer connectionTimeoutMs,
        @NotNull @Min(1000) @Max(60000) Integer readTimeoutMs,
        @Size(max = 500) String description
) {
}
