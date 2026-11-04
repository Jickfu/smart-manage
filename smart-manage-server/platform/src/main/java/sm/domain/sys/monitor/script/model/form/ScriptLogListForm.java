package sm.domain.sys.monitor.script.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

import java.time.LocalDateTime;

@Schema(description = "脚本执行日志列表输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class ScriptLogListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
    @Schema(description = "脚本执行状态：SUCCESS 成功、ERROR 错误、TIMEOUT 超时")
    private String status;
    @Schema(description = "脚本事务模式：ATOMIC 原子事务、NON_ATOMIC 非原子执行")
    private String transactionMode;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
