package sm.domain.sys.base.openapi.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpenApiCatalogTestForm(
        @NotNull Long releaseId,
        @NotNull Long applicationId,
        @NotBlank @Size(max = 65536) String requestJson) {
}
