package sm.domain.sys.monitor.script.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "脚本执行表单")
public class ScriptExecuteForm {
    @Schema(description = "关联的已保存脚本ID")
    private Long scriptId;

    @NotBlank(message = "脚本内容不能为空")
    @Size(max = 1000000, message = "脚本内容不能超过1000000个字符")
    @Schema(description = "脚本内容（JavaScript）")
    private String content;

    @Pattern(regexp = "ATOMIC|NON_ATOMIC", message = "事务模式不合法")
    @Schema(description = "事务模式：ATOMIC或NON_ATOMIC")
    private String transactionMode = "ATOMIC";
}
