package sm.domain.sys.base.basicdata.openapi.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BasicDataOpenApiQueryForm(
        @NotBlank @Size(max = 100) String categoryNumber) {
}
