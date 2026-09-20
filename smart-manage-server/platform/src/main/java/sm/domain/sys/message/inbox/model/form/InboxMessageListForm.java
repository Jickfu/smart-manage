package sm.domain.sys.message.inbox.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/** 管理端站内消息列表条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InboxMessageListForm extends PageForm {
    private String keyword;
    private String status;
    private String level;
}

