package sm.domain.sys.monitor.alert.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.message.email.contract.EmailNotificationSender;

/** 保护告警读回无偏移时间、持续时间判断与邮件重试调度。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class MonitorAlertTimePostgresTests {
  @Test
  void evaluatesAndRetriesUsingBeijingLocalTime() {
    var dataSource = new DriverManagerDataSource(
        System.getProperty("smartManage.testDbUrl"),
        System.getProperty("smartManage.testDbUser"),
        System.getProperty("smartManage.testDbPassword"));
    var jdbc = new JdbcTemplate(dataSource);
    var transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    transaction.executeWithoutResult(status -> {
      status.setRollbackOnly();
      long ruleId = jdbc.queryForObject(
          "SELECT id FROM t_sys_monitor_alert_rule WHERE rule_code='HOST_CPU_HIGH'", Long.class);
      jdbc.update("""
          INSERT INTO t_sys_monitor_alert_rule_recipient(id,rule_id,user_id,create_time)
          SELECT -9001000,?,id,LOCALTIMESTAMP FROM t_sys_user WHERE username='administrator'
          ON CONFLICT (rule_id,user_id) DO NOTHING
          """, ruleId);
      String hostId = "verify-alert-local-time";
      var evaluation = new MonitorAlertEvaluation(
          ruleId, "HOST_CPU_HIGH", "HOST", hostId, BigDecimal.ONE, new BigDecimal("0.9"),
          true, 60, null, 1800, true, "验证北京时间");
      var state = new MonitorAlertStateTxService(jdbc);
      state.evaluate(evaluation);
      jdbc.update("""
          UPDATE t_sys_monitor_alert_incident
          SET started_at=LOCALTIMESTAMP-interval '2 minutes' WHERE scope_id=?
          """, hostId);
      state.evaluate(evaluation);
      assertEquals("FIRING", jdbc.queryForObject(
          "SELECT status FROM t_sys_monitor_alert_incident WHERE scope_id=?", String.class, hostId));

      // 模拟告警事务已经提交，通知对后续分派可见。
      jdbc.update("""
          UPDATE t_sys_monitor_alert_notification SET next_attempt_time=LOCALTIMESTAMP
          WHERE incident_id=(SELECT id FROM t_sys_monitor_alert_incident WHERE scope_id=?)
          """, hostId);
      var emailSender = mock(EmailNotificationSender.class);
      when(emailSender.enqueue(any())).thenThrow(new IllegalStateException("验证重试"));
      new MonitorAlertNotifier(jdbc, emailSender, new MonitorMetricValueFormatter()).dispatch();
      verify(emailSender).enqueue(any());
      assertEquals("PENDING", jdbc.queryForObject("""
          SELECT notification.status FROM t_sys_monitor_alert_notification notification
          JOIN t_sys_monitor_alert_incident incident ON incident.id=notification.incident_id
          WHERE incident.scope_id=?
          """, String.class, hostId));
      var expected = jdbc.queryForObject(
          "SELECT LOCALTIMESTAMP + interval '5 minutes'", LocalDateTime.class);
      assertEquals(expected, jdbc.queryForObject("""
          SELECT notification.next_attempt_time FROM t_sys_monitor_alert_notification notification
          JOIN t_sys_monitor_alert_incident incident ON incident.id=notification.incident_id
          WHERE incident.scope_id=?
          """, LocalDateTime.class, hostId));
    });
  }
}
