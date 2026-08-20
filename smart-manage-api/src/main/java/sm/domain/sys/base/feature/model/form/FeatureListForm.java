package sm.domain.sys.base.feature.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeatureListForm extends PageForm {
    private Long appId;
    private Long domainId;
    @Schema(description = "关键词")
    private String keyword;
}
