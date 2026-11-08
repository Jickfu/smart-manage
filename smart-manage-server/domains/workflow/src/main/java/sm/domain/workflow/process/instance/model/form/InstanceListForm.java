package sm.domain.workflow.process.instance.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流程实例列表查询条件")
public class InstanceListForm extends PageForm {
    @Schema(description = "列表快速搜索关键字")
    @Size(max = 100)
    private String keyword;
}
