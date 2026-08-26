package sm.domain.sys.monitor.alert.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.monitor.alert.model.form.MonitorAlertRuleSaveForm;

class MonitorAlertTxServiceTests {
  @Test
  void disablingRuleClosesIncidentsAndSkipsUnsentFaultNotifications() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    when(jdbcTemplate.queryForMap(anyString(), any(Object[].class)))
        .thenReturn(
            Map.of(
                "rule_code",
                "HOST_CPU_HIGH",
                "value_kind",
                "RATIO",
                "min_value",
                BigDecimal.ZERO,
                "max_value",
                BigDecimal.ONE));
    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
    MonitorAlertRuleSaveForm form =
        new MonitorAlertRuleSaveForm(
            1L,
            0,
            false,
            "WARNING",
            BigDecimal.valueOf(.9),
            60,
            BigDecimal.valueOf(.8),
            1800,
            false,
            List.of(),
            "test");

    new MonitorAlertTxService(jdbcTemplate).saveRule(form, 1L);

    List<String> statements =
        mockingDetails(jdbcTemplate).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals("update"))
            .map(invocation -> String.valueOf((Object) invocation.getArgument(0)))
            .toList();
    assertTrue(statements.stream().anyMatch(sql -> sql.contains("close_reason='RULE_DISABLED'")));
    assertTrue(
        statements.stream()
            .anyMatch(sql -> sql.contains("alert_notification") && sql.contains("SKIPPED")));
  }
}
