package sm.domain.sys.monitor.script.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "脚本删除输入表单")
@Data
public class ScriptDeleteForm {
    @Schema(description = "ID")
    @NotNull(message = "脚本ID不能为空")
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull(message = "乐观锁版本号不能为空")
    private Integer version;
}
