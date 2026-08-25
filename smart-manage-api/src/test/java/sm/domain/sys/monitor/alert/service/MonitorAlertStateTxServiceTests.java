package sm.domain.sys.monitor.alert.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MonitorAlertStateTxServiceTests {
    @Test
    void firstViolationCreatesPendingIncidentWithoutSendingEarlyEmail() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        MonitorAlertStateTxService service = new MonitorAlertStateTxService(jdbcTemplate);

        service.evaluate(evaluation(true, true, 300));

        List<String> sql = updateSql(jdbcTemplate);
        assertTrue(sql.stream().anyMatch(statement -> statement.contains("INSERT INTO t_sys_monitor_alert_incident")));
        assertTrue(sql.stream().noneMatch(statement -> statement.contains("t_sys_monitor_alert_notification")));
    }

    @Test
    void firingIncidentRecoveryCreatesExactlyOneRecoveryOutboxEntry() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Map<String,Object> active = new HashMap<>();
        active.put("id", 10L); active.put("status", "FIRING"); active.put("peak_value", BigDecimal.ONE);
        active.put("started_at", Timestamp.from(Instant.now().minusSeconds(600)));
        active.put("last_notified_at", Timestamp.from(Instant.now().minusSeconds(60)));
        active.put("notification_count", 1);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(active));
        MonitorAlertStateTxService service = new MonitorAlertStateTxService(jdbcTemplate);

        service.evaluate(evaluation(false, true, 300));

        List<String> sql = updateSql(jdbcTemplate);
        assertTrue(sql.stream().anyMatch(statement -> statement.contains("status='RECOVERED'")));
        assertTrue(sql.stream().filter(statement -> statement.contains("t_sys_monitor_alert_notification")).count() == 1);
    }

    private MonitorAlertEvaluation evaluation(boolean violation, boolean emailEnabled, int duration) {
        return new MonitorAlertEvaluation(1, "HOST_CPU_HIGH", "HOST", "server-a",
                violation ? BigDecimal.valueOf(0.95) : BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.9),
                violation, duration, BigDecimal.valueOf(0.8), 1800, emailEnabled, "CPU 告警");
    }

    private List<String> updateSql(JdbcTemplate jdbcTemplate) {
        return mockingDetails(jdbcTemplate).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("update"))
                .map(invocation -> String.valueOf((Object) invocation.getArgument(0))).toList();
    }
}
