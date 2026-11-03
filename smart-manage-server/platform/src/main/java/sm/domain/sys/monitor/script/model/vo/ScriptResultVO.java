package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "脚本执行结果")
public class ScriptResultVO {
    @Schema(description = "脚本执行状态：SUCCESS 成功、ERROR 错误、TIMEOUT 超时")
    private String status;

    @Schema(description = "执行输出")
    private String output;

    @Schema(description = "执行耗时（毫秒）")
    private Integer executeDuration;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "输出是否被截断")
    private Boolean truncated;

    @Schema(description = "事务结果：COMMITTED 已提交、ROLLED_BACK 已回滚、NOT_APPLICABLE 不适用")
    private String transactionResult;
}
