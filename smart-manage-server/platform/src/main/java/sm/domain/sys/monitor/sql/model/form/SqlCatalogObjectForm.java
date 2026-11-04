package sm.domain.sys.monitor.sql.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "数据库对象目录对象输入表单")
@Data
public class SqlCatalogObjectForm {
    @Schema(description = "数据库模式名")
    @NotBlank
    @Size(max = 128)
    private String schemaName;

    @Schema(description = "数据对象名")
    @NotBlank
    @Size(max = 128)
    private String objectName;
}
