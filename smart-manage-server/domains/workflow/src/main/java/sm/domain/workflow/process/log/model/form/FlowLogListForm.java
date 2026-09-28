package sm.domain.workflow.process.log.model.form;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class FlowLogListForm extends PageForm {
    @Size(max = 100)
    private String keyword;
}
