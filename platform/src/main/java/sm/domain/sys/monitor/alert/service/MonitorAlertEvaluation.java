package sm.domain.sys.monitor.alert.service;

import java.math.BigDecimal;

record MonitorAlertEvaluation(long ruleId, String ruleCode, String scopeType, String scopeId,
                              BigDecimal value, BigDecimal threshold, boolean violation,
                              int durationSeconds, BigDecimal recoveryThreshold,
                              int repeatIntervalSeconds, boolean emailEnabled, String summary) { }
