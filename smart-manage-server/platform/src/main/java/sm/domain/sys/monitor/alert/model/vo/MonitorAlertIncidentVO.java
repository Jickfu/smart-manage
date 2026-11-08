package sm.domain.sys.monitor.alert.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "监控告警事件输出模型")
public record MonitorAlertIncidentVO(
    @Schema(description = "ID")
    Long id,
    @Schema(description = "规则编码")
    String ruleCode,
    @Schema(description = "告警规则名称")
    String ruleName,
    @Schema(description = "告警严重程度：WARNING 警告、CRITICAL 严重")
    String severity,
    @Schema(description = "监控作用对象类型：HOST 主机、INSTANCE 应用实例")
    String scopeType,
    @Schema(description = "作用对象标识")
    String scopeId,
    @Schema(description = "告警状态：PENDING、FIRING、RECOVERED、CLOSED")
    String status,
    @Schema(description = "非恢复性关闭原因：条件清除、实例退役、规则停用或指标不可用")
    String closeReason,
    @Schema(description = "异常开始时间")
    OffsetDateTime startedAt,
    @Schema(description = "触发时间")
    OffsetDateTime firedAt,
    @Schema(description = "恢复时间")
    OffsetDateTime recoveredAt,
    @Schema(description = "最近指标值，使用该规则的原始单位，展示文本见对应 Display 字段")
    BigDecimal lastValue,
    @Schema(description = "峰值指标值，使用该规则的原始单位，展示文本见对应 Display 字段")
    BigDecimal peakValue,
    @Schema(description = "最近指标值的展示文本，包含单位")
    String lastValueDisplay,
    @Schema(description = "峰值指标的展示文本，包含单位")
    String peakValueDisplay,
    @Schema(description = "摘要")
    String summary) {}
