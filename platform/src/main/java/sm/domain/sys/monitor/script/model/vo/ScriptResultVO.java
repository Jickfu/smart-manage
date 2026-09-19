package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "脚本执行结果")
public class ScriptResultVO {
    @Schema(description = "执行状态：SUCCESS、ERROR或TIMEOUT")
    private String status;

    @Schema(description = "执行输出")
    private String output;

    @Schema(description = "执行耗时（毫秒）")
    private Integer executeDuration;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "输出是否被截断")
    private Boolean truncated;

    @Schema(description = "事务结果：COMMITTED、ROLLED_BACK或NOT_APPLICABLE")
    private String transactionResult;
}
