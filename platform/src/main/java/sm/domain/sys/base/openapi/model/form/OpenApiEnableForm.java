package sm.domain.sys.base.openapi.model.form;

import jakarta.validation.constraints.NotNull;

public record OpenApiEnableForm(@NotNull Long id, @NotNull Integer version, @NotNull Boolean enabled) {
}
