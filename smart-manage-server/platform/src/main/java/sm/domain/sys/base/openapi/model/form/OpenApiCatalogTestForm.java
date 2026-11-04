package sm.domain.sys.base.openapi.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "开放接口目录测试输入表单")
public record OpenApiCatalogTestForm(
    @Schema(description = "待试调的 API 版本ID")
    @NotNull Long releaseId,
    @Schema(description = "接入应用 ID")
    @NotNull Long applicationId,
    @Schema(description = "试调请求 JSON 正文")
    @NotBlank @Size(max = 65536) String requestJson) {
}
