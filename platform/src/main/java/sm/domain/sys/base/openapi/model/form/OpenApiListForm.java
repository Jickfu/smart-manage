package sm.domain.sys.base.openapi.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class OpenApiListForm extends PageForm {
    private String keyword;
    private Boolean enabled;
    private Long applicationId;
    private String operationKey;
    private String resultType;
    private String domainKey;
    private String applicationKey;
    private String featureKey;
}
