package sm.domain.sys.monitor.sql.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SqlCatalogObjectForm {
    @NotBlank
    @Size(max = 128)
    private String schemaName;

    @NotBlank
    @Size(max = 128)
    private String objectName;
}
