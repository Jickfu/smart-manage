package sm.domain.sys.base.openapi.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record OpenApiCredentialCreateForm(@NotNull Long applicationId,
                                          @NotBlank @Size(max = 200) String name,
                                          LocalDateTime expiresAt) {
}
