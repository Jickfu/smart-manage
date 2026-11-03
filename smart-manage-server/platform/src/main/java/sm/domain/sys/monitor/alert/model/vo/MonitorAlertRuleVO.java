package sm.domain.sys.monitor.alert.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "监控告警规则输出模型")
public record MonitorAlertRuleVO(
    @Schema(description = "ID")
    Long id,
    @Schema(description = "规则编码")
    String ruleCode,
    @Schema(description = "名称")
    String name,
    @Schema(description = "监控作用对象类型：HOST 主机、INSTANCE 应用实例")
    String scopeType,
    @Schema(description = "是否启用")
    boolean enabled,
    @Schema(description = "告警严重程度：WARNING 警告、CRITICAL 严重")
    String severity,
    @Schema(description = "触发阈值，使用 valueKind 定义的原始单位；RATIO 为 0～1 比值")
    BigDecimal threshold,
    @Schema(description = "持续时间秒数")
    int durationSeconds,
    @Schema(description = "恢复阈值，与触发阈值使用相同原始单位")
    BigDecimal recoveryThreshold,
    @Schema(description = "重复通知间隔秒数")
    int repeatIntervalSeconds,
    @Schema(description = "是否发送邮件")
    boolean emailEnabled,
    @Schema(description = "说明")
    String description,
    @Schema(description = "乐观锁版本号")
    int version,
    @Schema(description = "值类型：RATIO/COUNT/BOOLEAN/RATE/DURATION_MS")
    String valueKind,
    @Schema(description = "展示单位")
    String displayUnit,
    @Schema(description = "最小可配置值")
    BigDecimal minValue,
    @Schema(description = "最大可配置值")
    BigDecimal maxValue,
    @Schema(description = "推荐触发阈值")
    BigDecimal recommendedThreshold,
    @Schema(description = "告警邮件接收用户引用列表")
    List<UserRef> recipientUsers) {
  @Schema(description = "接收用户引用")
  public record UserRef(
      @Schema(description = "ID")
      Long id,
      @Schema(description = "业务编码")
      String number,
      @Schema(description = "名称")
      String name) {}
}
