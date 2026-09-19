package sm.domain.sys.base.weakpassword.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class WeakPasswordListForm extends PageForm {
    private String keyword;
}
