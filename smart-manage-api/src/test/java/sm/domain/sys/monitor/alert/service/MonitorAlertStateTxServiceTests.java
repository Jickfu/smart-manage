package sm.domain.sys.monitor.alert.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class MonitorAlertStateTxServiceTests {
  @Test
  void firstViolationCreatesPendingIncidentWithoutSendingEarlyEmail() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    allowEvaluation(jdbcTemplate);
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
    MonitorAlertStateTxService service = new MonitorAlertStateTxService(jdbcTemplate);

    service.evaluate(evaluation(true, true, 300));

    List<String> sql = updateSql(jdbcTemplate);
    assertTrue(
        sql.stream()
            .anyMatch(statement -> statement.contains("INSERT INTO t_sys_monitor_alert_incident")));
    assertTrue(
        sql.stream()
            .noneMatch(statement -> statement.contains("t_sys_monitor_alert_notification")));
  }

  @Test
  void firingIncidentRecoveryCreatesExactlyOneRecoveryOutboxEntry() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    allowEvaluation(jdbcTemplate);
    Map<String, Object> active = new HashMap<>();
    active.put("id", 10L);
    active.put("status", "FIRING");
    active.put("peak_value", BigDecimal.ONE);
    active.put("started_at", Timestamp.from(Instant.now().minusSeconds(600)));
    active.put("last_notified_at", Timestamp.from(Instant.now().minusSeconds(60)));
    active.put("notification_count", 1);
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(active));
    MonitorAlertStateTxService service = new MonitorAlertStateTxService(jdbcTemplate);

    service.evaluate(evaluation(false, true, 300));

    List<String> sql = updateSql(jdbcTemplate);
    assertTrue(sql.stream().anyMatch(statement -> statement.contains("status='RECOVERED'")));
    assertTrue(
        sql.stream()
                .filter(statement -> statement.contains("t_sys_monitor_alert_notification"))
                .count()
            == 1);
  }

  @Test
  void pendingStopsWhenTriggerConditionClearsEvenAboveRecoveryThreshold() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    allowEvaluation(jdbcTemplate);
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
        .thenReturn(List.of(active("PENDING", null, 0)));
    MonitorAlertStateTxService service = new MonitorAlertStateTxService(jdbcTemplate);
    service.evaluate(
        new MonitorAlertEvaluation(
            1,
            "HOST_CPU_HIGH",
            "HOST",
            "server-a",
            BigDecimal.valueOf(.85),
            BigDecimal.valueOf(.9),
            false,
            300,
            BigDecimal.valueOf(.8),
            1800,
            true,
            "CPU 待触发取消"));
    List<String> sql = updateSql(jdbcTemplate);
    assertTrue(sql.stream().anyMatch(statement -> statement.contains("PENDING_CLEARED")));
    assertFalse(sql.stream().anyMatch(statement -> statement.contains("status='FIRING'")));
  }

  @Test
  void binaryZeroAndExactThresholdRecoverFiringIncident() {
    for (BigDecimal value : List.of(BigDecimal.ZERO, BigDecimal.valueOf(.8))) {
      JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
      allowEvaluation(jdbcTemplate);
      when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
          .thenReturn(List.of(active("FIRING", Instant.now(), 1)));
      new MonitorAlertStateTxService(jdbcTemplate)
          .evaluate(
              new MonitorAlertEvaluation(
                  1,
                  "TEST",
                  "INSTANCE",
                  "instance1",
                  value,
                  BigDecimal.ONE,
                  false,
                  0,
                  BigDecimal.valueOf(.8),
                  1800,
                  false,
                  "恢复"));
      assertTrue(
          updateSql(jdbcTemplate).stream()
              .anyMatch(statement -> statement.contains("status='RECOVERED'")));
    }
  }

  @Test
  void repeatNotificationOnlyOccursAfterConfiguredInterval() {
    JdbcTemplate early = mock(JdbcTemplate.class);
    allowEvaluation(early);
    when(early.queryForList(anyString(), any(Object[].class)))
        .thenReturn(List.of(active("FIRING", Instant.now().minusSeconds(60), 1)));
    new MonitorAlertStateTxService(early).evaluate(evaluation(true, true, 0));
    assertFalse(
        updateSql(early).stream()
            .anyMatch(statement -> statement.contains("t_sys_monitor_alert_notification")));

    JdbcTemplate due = mock(JdbcTemplate.class);
    allowEvaluation(due);
    when(due.queryForList(anyString(), any(Object[].class)))
        .thenReturn(List.of(active("FIRING", Instant.now().minusSeconds(1900), 1)));
    new MonitorAlertStateTxService(due).evaluate(evaluation(true, true, 0));
    assertTrue(
        updateSql(due).stream()
                .filter(statement -> statement.contains("t_sys_monitor_alert_notification"))
                .count()
            == 1);
  }

  @Test
  void unavailableMetricClosesPendingButKeepsFiringWithoutRecovery() {
    JdbcTemplate pending = mock(JdbcTemplate.class);
    new MonitorAlertStateTxService(pending).metricUnavailable(1, "HOST", "server-a");
    assertTrue(
        updateSql(pending).stream()
            .anyMatch(
                statement ->
                    statement.contains("METRIC_UNAVAILABLE")
                        && statement.contains("status='PENDING'")));
    assertFalse(updateSql(pending).stream().anyMatch(statement -> statement.contains("RECOVERED")));
  }

  private Map<String, Object> active(String status, Instant lastNotified, int count) {
    Map<String, Object> value = new HashMap<>();
    value.put("id", 10L);
    value.put("status", status);
    value.put("peak_value", BigDecimal.ONE);
    value.put("started_at", Timestamp.from(Instant.now().minusSeconds(600)));
    value.put("last_notified_at", lastNotified == null ? null : Timestamp.from(lastNotified));
    value.put("notification_count", count);
    return value;
  }

  private void allowEvaluation(JdbcTemplate jdbcTemplate) {
    when(jdbcTemplate.queryForList(contains("SELECT enabled"), eq(Boolean.class), anyLong()))
        .thenReturn(List.of(true));
    when(jdbcTemplate.queryForList(contains("SELECT lifecycle"), eq(String.class), anyString()))
        .thenReturn(List.of("ACTIVE"));
  }

  private MonitorAlertEvaluation evaluation(boolean violation, boolean emailEnabled, int duration) {
    return new MonitorAlertEvaluation(
        1,
        "HOST_CPU_HIGH",
        "HOST",
        "server-a",
        violation ? BigDecimal.valueOf(0.95) : BigDecimal.valueOf(0.2),
        BigDecimal.valueOf(0.9),
        violation,
        duration,
        BigDecimal.valueOf(0.8),
        1800,
        emailEnabled,
        "CPU 告警");
  }

  private List<String> updateSql(JdbcTemplate jdbcTemplate) {
    return mockingDetails(jdbcTemplate).getInvocations().stream()
        .filter(invocation -> invocation.getMethod().getName().equals("update"))
        .map(invocation -> String.valueOf((Object) invocation.getArgument(0)))
        .toList();
  }
}
