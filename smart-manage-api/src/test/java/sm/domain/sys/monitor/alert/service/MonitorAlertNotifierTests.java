package sm.domain.sys.monitor.alert.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.message.email.contract.EmailNotificationCommand;
import sm.domain.sys.message.email.contract.EmailNotificationSender;

class MonitorAlertNotifierTests {
  @Test
  void closedIncidentIsSkippedWithoutCallingEmailSender() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    when(jdbcTemplate.queryForMap(anyString(), eq(1L)))
        .thenReturn(notification("CLOSED", "FIRING"));
    EmailNotificationSender sender = mock(EmailNotificationSender.class);

    new MonitorAlertNotifier(jdbcTemplate, sender, new MonitorMetricValueFormatter()).dispatchOne(1L);

    verifyNoInteractions(sender);
    verify(jdbcTemplate)
        .update(contains("status='SKIPPED'"), anyString(), eq(1L));
  }

  @Test
  void emailUsesTheSameFormattedLastAndPeakValues() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    when(jdbcTemplate.queryForMap(anyString(), eq(2L)))
        .thenReturn(notification("FIRING", "FIRING"));
    when(jdbcTemplate.queryForList(anyString(), eq(Long.class), anyLong()))
        .thenReturn(List.of(10L));
    when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(2L))).thenReturn(1);
    EmailNotificationSender sender = mock(EmailNotificationSender.class);
    when(sender.enqueue(any())).thenReturn(20L);

    new MonitorAlertNotifier(jdbcTemplate, sender, new MonitorMetricValueFormatter()).dispatchOne(2L);

    ArgumentCaptor<EmailNotificationCommand> command =
        ArgumentCaptor.forClass(EmailNotificationCommand.class);
    verify(sender).enqueue(command.capture());
    assertTrue(command.getValue().textBody().contains("当前值：95.34%"));
    assertTrue(command.getValue().textBody().contains("峰值：97.35%"));
  }

  private Map<String, Object> notification(String incidentStatus, String notificationType) {
    Map<String, Object> value = new HashMap<>();
    value.put("incident_id", 100L);
    value.put("status", "PROCESSING");
    value.put("incident_status", incidentStatus);
    value.put("notification_type", notificationType);
    value.put("severity", "CRITICAL");
    value.put("rule_name", "CPU 过高");
    value.put("summary", "CPU 异常");
    value.put("scope_type", "HOST");
    value.put("scope_id", "server-a");
    value.put("started_at", "2026-08-26T10:00:00Z");
    value.put("last_value", new BigDecimal("0.9534"));
    value.put("peak_value", new BigDecimal("0.9735"));
    value.put("value_kind", "RATIO");
    value.put("display_unit", "%");
    return value;
  }
}
