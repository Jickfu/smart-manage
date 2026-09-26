package sm.domain.workflow.process.task.model.form;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;
import sm.domain.workflow.process.engine.WorkflowEngine;
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskListForm extends PageForm {
    @NotNull
    private WorkflowEngine.Box box = WorkflowEngine.Box.PENDING;
}
