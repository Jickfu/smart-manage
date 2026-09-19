package sm.domain.sys.monitor.alert.model.vo;

import java.math.BigDecimal;
import java.util.List;

public record MonitorAlertRuleVO(
    Long id,
    String ruleCode,
    String name,
    String scopeType,
    boolean enabled,
    String severity,
    BigDecimal threshold,
    int durationSeconds,
    BigDecimal recoveryThreshold,
    int repeatIntervalSeconds,
    boolean emailEnabled,
    String description,
    int version,
    String valueKind,
    String displayUnit,
    BigDecimal minValue,
    BigDecimal maxValue,
    BigDecimal recommendedThreshold,
    List<UserRef> recipientUsers) {
  public record UserRef(Long id, String number, String name) {}
}
