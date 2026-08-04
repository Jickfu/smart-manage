package sm.domain.sys.monitor.script.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScriptDeleteForm {
    @NotNull(message = "脚本ID不能为空")
    private Long id;
    @NotNull(message = "乐观锁版本号不能为空")
    private Integer version;
}
