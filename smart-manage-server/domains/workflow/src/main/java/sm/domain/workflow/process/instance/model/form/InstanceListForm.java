package sm.domain.workflow.process.instance.model.form;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstanceListForm extends PageForm {
    @Size(max = 100)
    private String keyword;
}
