package sm.domain.sys.base.openapi.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "开放接口目录导出输入表单")
public record OpenApiCatalogExportForm(
    @Schema(description = "ID 列表")
    @NotEmpty @Size(max = 100) List<Long> ids) {
}
