package sm.domain.sys.monitor.alert.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "监控告警规则保存输入表单")
public record MonitorAlertRuleSaveForm(
    @Schema(description = "ID")
    @NotNull Long id,
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull Integer version,
    @Schema(description = "是否启用")
    @NotNull Boolean enabled,
    @Schema(description = "告警严重程度：WARNING 警告、CRITICAL 严重")
    @NotBlank String severity,
    @Schema(description = "触发阈值，使用 valueKind 定义的原始单位；RATIO 为 0～1 比值")
    @NotNull @DecimalMin("0") BigDecimal threshold,
    @Schema(description = "持续时间秒数")
    @NotNull @Min(0) @Max(86400) Integer durationSeconds,
    @Schema(description = "恢复阈值，与触发阈值使用相同原始单位")
    @DecimalMin("0") BigDecimal recoveryThreshold,
    @Schema(description = "重复通知间隔秒数")
    @NotNull @Min(60) @Max(604800) Integer repeatIntervalSeconds,
    @Schema(description = "是否发送邮件")
    @NotNull Boolean emailEnabled,
    @Schema(description = "告警邮件接收用户 ID 列表")
    @Size(max = 50) List<Long> recipientUserIds,
    @Schema(description = "说明")
    @Size(max = 500) String description) { }
