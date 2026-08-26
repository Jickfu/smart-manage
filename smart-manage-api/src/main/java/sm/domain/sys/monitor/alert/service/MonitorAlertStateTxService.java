package sm.domain.sys.monitor.alert.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class MonitorAlertStateTxService {
  private final JdbcTemplate jdbcTemplate;

  void evaluate(MonitorAlertEvaluation evaluation) {
    Instant now = Instant.now();
    List<Map<String, Object>> active =
        jdbcTemplate.queryForList(
            """
SELECT * FROM t_sys_monitor_alert_incident WHERE rule_id=? AND scope_type=? AND scope_id=?
AND status IN ('PENDING','FIRING') FOR UPDATE
""",
            evaluation.ruleId(),
            evaluation.scopeType(),
            evaluation.scopeId());
    if (active.isEmpty()) {
      if (evaluation.violation()) createPending(evaluation, now);
      return;
    }
    Map<String, Object> incident = active.getFirst();
    long incidentId = ((Number) incident.get("id")).longValue();
    String status = (String) incident.get("status");
    BigDecimal lastValue = evaluation.value();
    BigDecimal peak = max(decimal(incident.get("peak_value")), lastValue);
    if ("PENDING".equals(status) && !evaluation.violation()) {
      jdbcTemplate.update(
          """
          UPDATE t_sys_monitor_alert_incident SET status='CLOSED',close_reason='PENDING_CLEARED',
          last_evaluated_at=?,last_value=?,peak_value=?,summary=?,version=version+1 WHERE id=?
          """,
          jdbcTime(now),
          lastValue,
          peak,
          evaluation.summary(),
          incidentId);
      return;
    }
    boolean recovered =
        "FIRING".equals(status)
            && (evaluation.recoveryThreshold() == null
                ? !evaluation.violation()
                : evaluation.value().compareTo(evaluation.recoveryThreshold()) <= 0);
    if (recovered) {
      jdbcTemplate.update(
          """
UPDATE t_sys_monitor_alert_incident SET status='RECOVERED',recovered_at=?,last_evaluated_at=?,
last_value=?,peak_value=?,summary=?,version=version+1 WHERE id=?
""",
          jdbcTime(now),
          jdbcTime(now),
          lastValue,
          peak,
          evaluation.summary(),
          incidentId);
      if (evaluation.emailEnabled()) enqueue(incidentId, "RECOVERY", 1, now);
      return;
    }
    Instant startedAt = instant(incident.get("started_at"));
    if ("PENDING".equals(status)
        && !now.isBefore(startedAt.plusSeconds(evaluation.durationSeconds()))) {
      jdbcTemplate.update(
          """
UPDATE t_sys_monitor_alert_incident SET status='FIRING',fired_at=?,last_evaluated_at=?,last_value=?,
peak_value=?,summary=?,last_notified_at=?,notification_count=?,version=version+1 WHERE id=?
""",
          jdbcTime(now),
          jdbcTime(now),
          lastValue,
          peak,
          evaluation.summary(),
          evaluation.emailEnabled() ? jdbcTime(now) : null,
          evaluation.emailEnabled() ? 1 : 0,
          incidentId);
      if (evaluation.emailEnabled()) enqueue(incidentId, "FIRING", 1, now);
      return;
    }
    Instant lastNotifiedAt = instant(incident.get("last_notified_at"));
    int notificationCount = ((Number) incident.get("notification_count")).intValue();
    boolean repeat =
        "FIRING".equals(status)
            && evaluation.emailEnabled()
            && (lastNotifiedAt == null
                || !now.isBefore(lastNotifiedAt.plusSeconds(evaluation.repeatIntervalSeconds())));
    jdbcTemplate.update(
        """
UPDATE t_sys_monitor_alert_incident SET last_evaluated_at=?,last_value=?,peak_value=?,summary=?,
last_notified_at=?,notification_count=?,version=version+1 WHERE id=?
""",
        jdbcTime(now),
        lastValue,
        peak,
        evaluation.summary(),
        repeat ? jdbcTime(now) : jdbcTime(lastNotifiedAt),
        repeat ? notificationCount + 1 : notificationCount,
        incidentId);
    if (repeat)
      enqueue(incidentId, lastNotifiedAt == null ? "FIRING" : "REPEAT", notificationCount + 1, now);
  }

  private void createPending(MonitorAlertEvaluation evaluation, Instant now) {
    long incidentId = IdWorker.getId();
    String cycleKey = now.toString() + ":" + incidentId;
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_alert_incident(id,rule_id,rule_code,scope_type,scope_id,status,cycle_key,
started_at,last_evaluated_at,last_value,peak_value,threshold,notification_count,summary,version)
VALUES(?,?,?,?,?,'PENDING',?,?,?,?,?,?,0,?,0)
""",
        incidentId,
        evaluation.ruleId(),
        evaluation.ruleCode(),
        evaluation.scopeType(),
        evaluation.scopeId(),
        cycleKey,
        jdbcTime(now),
        jdbcTime(now),
        evaluation.value(),
        evaluation.value(),
        evaluation.threshold(),
        evaluation.summary());
    if (evaluation.durationSeconds() == 0) {
      jdbcTemplate.update(
          """
          UPDATE t_sys_monitor_alert_incident SET status='FIRING',fired_at=?,last_notified_at=?,
          notification_count=?,version=version+1 WHERE id=?
          """,
          jdbcTime(now),
          evaluation.emailEnabled() ? jdbcTime(now) : null,
          evaluation.emailEnabled() ? 1 : 0,
          incidentId);
      if (evaluation.emailEnabled()) enqueue(incidentId, "FIRING", 1, now);
    }
  }

  private void enqueue(long incidentId, String type, int sequence, Instant now) {
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_alert_notification(id,incident_id,notification_type,sequence_no,status,
attempt_count,next_attempt_time,create_time) VALUES(?,?,?,?,'PENDING',0,?,?)
""",
        IdWorker.getId(),
        incidentId,
        type,
        sequence,
        jdbcTime(now),
        jdbcTime(now));
  }

  private BigDecimal decimal(Object value) {
    return value instanceof BigDecimal decimal
        ? decimal
        : value == null ? null : new BigDecimal(value.toString());
  }

  private BigDecimal max(BigDecimal first, BigDecimal second) {
    if (first == null) return second;
    if (second == null) return first;
    return first.max(second);
  }

  private Instant instant(Object value) {
    if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
    if (value instanceof OffsetDateTime offset) return offset.toInstant();
    return value instanceof Instant instant ? instant : null;
  }

  private OffsetDateTime jdbcTime(Instant value) {
    return value == null ? null : OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
  }
}
