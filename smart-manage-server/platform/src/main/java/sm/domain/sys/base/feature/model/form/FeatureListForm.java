package sm.domain.sys.base.feature.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Schema(description = "功能列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class FeatureListForm extends PageForm {
    @Schema(description = "所属应用ID")
    private Long appId;
    @Schema(description = "所属领域ID")
    private Long domainId;
    @Schema(description = "关键词")
    private String keyword;
}
