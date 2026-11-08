package sm.domain.workflow.process.log.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流转日志查询条件")
public class FlowLogListForm extends PageForm {
    @Schema(description = "列表快速搜索关键字")
    @Size(max = 100)
    private String keyword;
}
