package sm.domain.workflow.process.task.model.form;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskMonitorListForm extends PageForm {
    @Size(max = 100)
    private String keyword;
}
