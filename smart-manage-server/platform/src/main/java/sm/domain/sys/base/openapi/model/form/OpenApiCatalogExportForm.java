package sm.domain.sys.base.openapi.model.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OpenApiCatalogExportForm(
        @NotEmpty @Size(max = 100) List<Long> ids) {
}
