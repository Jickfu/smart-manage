package sm.domain.sys.monitor.alert.model.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MonitorAlertIncidentVO(
    Long id,
    String ruleCode,
    String ruleName,
    String severity,
    String scopeType,
    String scopeId,
    String status,
    String closeReason,
    OffsetDateTime startedAt,
    OffsetDateTime firedAt,
    OffsetDateTime recoveredAt,
    BigDecimal lastValue,
    BigDecimal peakValue,
    String lastValueDisplay,
    String peakValueDisplay,
    String summary) {}
