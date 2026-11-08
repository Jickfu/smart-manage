package sm.domain.workflow.process.task.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;
import sm.domain.workflow.process.engine.WorkflowEngine;
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "个人任务列表查询条件")
public class TaskListForm extends PageForm {
    @Schema(description = "个人任务分类，默认待办")
    @NotNull
    private WorkflowEngine.Box box = WorkflowEngine.Box.PENDING;
}
