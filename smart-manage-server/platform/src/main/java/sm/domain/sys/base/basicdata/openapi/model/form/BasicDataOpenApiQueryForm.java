package sm.domain.sys.base.basicdata.openapi.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "基础资料开放接口查询输入表单")
public record BasicDataOpenApiQueryForm(
    @Schema(description = "基础资料分类编码")
    @NotBlank @Size(max = 100) String categoryNumber) {
}
