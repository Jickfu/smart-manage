package sm.domain.sys.base.openapi.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OpenApiReleaseStatusForm(@NotNull Long id, @NotNull Integer version,
                                       @NotBlank String status) {
}
