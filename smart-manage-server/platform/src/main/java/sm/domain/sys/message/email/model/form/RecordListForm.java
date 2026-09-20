package sm.domain.sys.message.email.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class RecordListForm extends PageForm {
    private String keyword;
    private String status;
    private Long accountId;
}
