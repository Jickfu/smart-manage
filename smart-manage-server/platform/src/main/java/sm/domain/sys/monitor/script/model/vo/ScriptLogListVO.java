package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "脚本执行日志列表输出模型")
@Data
public class ScriptLogListVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "关联脚本ID")
    private Long scriptId;
    @Schema(description = "脚本名称快照")
    private String scriptName;
    @Schema(description = "脚本事务模式：ATOMIC 原子事务、NON_ATOMIC 非原子执行")
    private String transactionMode;
    @Schema(description = "脚本执行状态：SUCCESS 成功、ERROR 错误、TIMEOUT 超时")
    private String executeStatus;
    @Schema(description = "执行耗时(ms)")
    private Integer executeDuration;
    @Schema(description = "事务结果：COMMITTED 已提交、ROLLED_BACK 已回滚、NOT_APPLICABLE 不适用")
    private String transactionResult;
    @Schema(description = "执行人")
    private String createName;
    @Schema(description = "执行IP")
    private String createIp;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
