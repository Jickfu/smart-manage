package sm.domain.sys.base.org.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrgParentListForm extends PageForm {
    private Long parentId;
    private Long excludedId;
    private String keyword;
}
