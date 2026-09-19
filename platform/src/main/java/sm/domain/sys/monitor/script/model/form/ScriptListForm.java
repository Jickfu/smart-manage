package sm.domain.sys.monitor.script.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "脚本列表表单")
public class ScriptListForm extends PageForm {
    @Schema(description = "关键词")
    private String keyword;
}
