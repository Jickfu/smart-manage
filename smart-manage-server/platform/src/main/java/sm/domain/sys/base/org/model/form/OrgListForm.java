package sm.domain.sys.base.org.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrgListForm extends PageForm {
    private Long parentId;
    private Boolean includeDescendants;
    private Boolean showArchived;
    private String keyword;
}
