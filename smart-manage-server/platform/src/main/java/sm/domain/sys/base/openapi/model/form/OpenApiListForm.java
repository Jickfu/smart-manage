package sm.domain.sys.base.openapi.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "开放接口列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpenApiListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "接入应用 ID")
    private Long applicationId;
    @Schema(description = "API 操作的稳定标识")
    private String operationKey;
    @Schema(description = "API 调用结果：SUCCESS、BUSINESS_FAILED、AUTHENTICATION_FAILED、ACCESS_DENIED、SYSTEM_FAILED")
    private String resultType;
    @Schema(description = "API 归属领域稳定编码")
    private String domainKey;
    @Schema(description = "API 归属应用稳定编码")
    private String applicationKey;
    @Schema(description = "稳定功能键")
    private String featureKey;
}
