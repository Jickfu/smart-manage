package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "脚本执行日志详情输出模型")
@Data
@EqualsAndHashCode(callSuper = true)
public class ScriptLogDetailVO extends ScriptLogListVO {
    @Schema(description = "实际执行脚本快照")
    private String scriptContent;
    @Schema(description = "截断后的执行输出")
    private String output;
    @Schema(description = "错误说明")
    private String errorMessage;
}
