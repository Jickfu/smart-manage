package sm.domain.sys.message.email.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccountListForm extends PageForm {
    private String keyword;
    private Boolean enabled;
}
