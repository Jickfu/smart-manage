package sm.domain.sys.monitor.alert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sm.domain.sys.message.email.contract.*;
import java.time.Instant;
import java.util.*;

/** 告警事务只写发件箱；本组件提交事务后再幂等创建邮件任务。 */
@Component
@RequiredArgsConstructor
@Slf4j
class MonitorAlertNotifier {
    private final JdbcTemplate jdbcTemplate;
    private final EmailNotificationSender emailSender;

    @Scheduled(fixedDelayString = "${smart-manage.monitor.alert-notification-interval-ms:10000}")
    void dispatch() {
        recoverClaims();
        for (Long notificationId : claim(20)) dispatchOne(notificationId);
    }

    private List<Long> claim(int limit) {
        return jdbcTemplate.queryForList("""
                UPDATE t_sys_monitor_alert_notification SET status='PROCESSING',claimed_time=now(),attempt_count=attempt_count+1
                WHERE id IN (SELECT id FROM t_sys_monitor_alert_notification WHERE status='PENDING' AND next_attempt_time<=now()
                ORDER BY id FOR UPDATE SKIP LOCKED LIMIT ?) RETURNING id
                """, Long.class, limit);
    }

    private void dispatchOne(Long notificationId) {
        try {
            Map<String,Object> notification = jdbcTemplate.queryForMap("""
                    SELECT a.*,b.rule_code,b.scope_type,b.scope_id,b.status incident_status,b.summary,b.started_at,b.fired_at,
                    b.recovered_at,b.last_value,b.peak_value,c.name rule_name,c.severity
                    FROM t_sys_monitor_alert_notification a JOIN t_sys_monitor_alert_incident b ON b.id=a.incident_id
                    JOIN t_sys_monitor_alert_rule c ON c.id=b.rule_id WHERE a.id=?
                    """, notificationId);
            List<Long> recipients = jdbcTemplate.queryForList("""
                    SELECT user_id FROM t_sys_monitor_alert_rule_recipient WHERE rule_id=(
                    SELECT rule_id FROM t_sys_monitor_alert_incident WHERE id=?) ORDER BY user_id
                    """, Long.class, ((Number) notification.get("incident_id")).longValue());
            if (recipients.isEmpty()) { markSkipped(notificationId, "告警规则未配置邮件接收人"); return; }
            String type = (String) notification.get("notification_type");
            String subject = "[Smart Manage][" + notification.get("severity") + "] "
                    + ("RECOVERY".equals(type) ? "已恢复：" : "告警：") + notification.get("rule_name");
            String text = notification.get("summary") + "\n对象：" + notification.get("scope_type") + "/" + notification.get("scope_id")
                    + "\n开始时间：" + notification.get("started_at") + "\n峰值：" + notification.get("peak_value");
            String html = "<p>" + escape((String) notification.get("summary")) + "</p><p>对象："
                    + escape(notification.get("scope_type") + "/" + notification.get("scope_id")) + "</p><p>开始时间："
                    + escape(String.valueOf(notification.get("started_at"))) + "</p><p>峰值：" + escape(String.valueOf(notification.get("peak_value"))) + "</p>";
            Long taskId = emailSender.enqueue(new EmailNotificationCommand("monitor.alert",
                    "monitor.alert:" + notificationId, recipients, subject, html, text));
            jdbcTemplate.update("UPDATE t_sys_monitor_alert_notification SET status='SUCCESS',email_task_id=?,completed_time=now(),error_message=NULL WHERE id=? AND status='PROCESSING'", taskId, notificationId);
        } catch (Exception exception) {
            String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            jdbcTemplate.update("""
                    UPDATE t_sys_monitor_alert_notification SET status=CASE WHEN attempt_count>=10 THEN 'FAILED' ELSE 'PENDING' END,
                    next_attempt_time=now()+interval '5 minutes',error_message=?,claimed_time=NULL WHERE id=? AND status='PROCESSING'
                    """, message.substring(0, Math.min(500, message.length())), notificationId);
            log.warn("监控告警邮件入队失败: notificationId={}", notificationId, exception);
        }
    }

    private void recoverClaims() {
        jdbcTemplate.update("""
                UPDATE t_sys_monitor_alert_notification SET status='PENDING',claimed_time=NULL,next_attempt_time=now()
                WHERE status='PROCESSING' AND claimed_time<now()-interval '5 minutes'
                """);
    }
    private void markSkipped(Long id, String reason) { jdbcTemplate.update("UPDATE t_sys_monitor_alert_notification SET status='SKIPPED',completed_time=now(),error_message=? WHERE id=?", reason, id); }
    private String escape(String value) { return value == null ? "" : value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;"); }
}
