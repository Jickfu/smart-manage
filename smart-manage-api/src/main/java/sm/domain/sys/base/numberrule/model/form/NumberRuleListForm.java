package sm.domain.sys.base.numberrule.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class NumberRuleListForm extends PageForm {
    private String keyword;
    private String scopeType;
    private Long cloudId;
    private Long appId;
    private Long featureId;
    private String referenceKey;
    private Boolean enabled;
}
