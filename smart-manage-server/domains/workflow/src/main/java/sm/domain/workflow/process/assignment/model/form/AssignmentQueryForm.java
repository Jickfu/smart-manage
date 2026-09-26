package sm.domain.workflow.process.assignment.model.form;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Size;
import sm.system.form.PageForm;
@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentQueryForm extends PageForm {
    @Size(max = 20) private String handlerType = "用户";
    @Size(max = 100) private String handlerName;
    @Size(max = 100) private String handlerCode;
}
