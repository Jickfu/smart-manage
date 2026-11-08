package sm.domain.workflow.process.assignment.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Size;
import sm.system.form.PageForm;
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "审批人候选查询条件")
public class AssignmentQueryForm extends PageForm {
    @Schema(description = "候选类型，支持用户、组织角色和组织负责人")
    @Size(max = 20) private String handlerType = "用户";
    @Schema(description = "候选名称搜索条件")
    @Size(max = 100) private String handlerName;
    @Schema(description = "候选编码搜索条件")
    @Size(max = 100) private String handlerCode;
}
