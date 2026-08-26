package sm.domain.sys.monitor.alert.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.monitor.alert.model.form.MonitorAlertRuleSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class MonitorAlertTxService {
  private static final Set<String> SEVERITIES = Set.of("INFO", "WARNING", "CRITICAL");
  private final JdbcTemplate jdbcTemplate;

  void saveRule(MonitorAlertRuleSaveForm form, Long userId) {
    if (!SEVERITIES.contains(form.severity()))
      throw new BizException(ResultEnum.PARAM_ERROR, "告警严重程度不合法");
    Map<String, Object> metadata =
        jdbcTemplate.queryForMap(
            "SELECT rule_code,value_kind,min_value,max_value FROM t_sys_monitor_alert_rule WHERE"
                + " id=?",
            form.id());
    validateThreshold(metadata, form);
    if (form.recoveryThreshold() != null
        && form.recoveryThreshold().compareTo(form.threshold()) > 0) {
      throw new BizException(ResultEnum.PARAM_ERROR, "恢复阈值不能高于触发阈值");
    }
    int changed =
        jdbcTemplate.update(
            """
UPDATE t_sys_monitor_alert_rule SET enabled=?,severity=?,threshold=?,duration_seconds=?,
recovery_threshold=?,repeat_interval_seconds=?,email_enabled=?,description=?,update_time=?,update_user=?,version=version+1
WHERE id=? AND version=?
""",
            form.enabled(),
            form.severity(),
            form.threshold(),
            form.durationSeconds(),
            form.recoveryThreshold(),
            form.repeatIntervalSeconds(),
            form.emailEnabled(),
            trim(form.description()),
            OffsetDateTime.now(ZoneOffset.UTC),
            userId,
            form.id(),
            form.version());
    if (changed != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "告警规则不存在或已被其他用户修改");
    if (!form.enabled()) {
      jdbcTemplate.update(
          """
          UPDATE t_sys_monitor_alert_incident SET status='CLOSED',close_reason='RULE_DISABLED',
          last_evaluated_at=now(),version=version+1
          WHERE rule_id=? AND status IN ('PENDING','FIRING')
          """,
          form.id());
      jdbcTemplate.update(
          """
          UPDATE t_sys_monitor_alert_notification SET status='SKIPPED',completed_time=now(),
          error_message='告警规则已停用',claimed_time=NULL
          WHERE notification_type IN ('FIRING','REPEAT') AND status IN ('PENDING','PROCESSING')
          AND incident_id IN (SELECT id FROM t_sys_monitor_alert_incident WHERE rule_id=?)
          """,
          form.id());
    }
    jdbcTemplate.update(
        "DELETE FROM t_sys_monitor_alert_rule_recipient WHERE rule_id=?", form.id());
    if (form.recipientUserIds() != null) {
      for (Long recipientUserId : new LinkedHashSet<>(form.recipientUserIds())) {
        Integer valid =
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM t_sys_user WHERE id=? AND enabled=true AND email IS NOT NULL"
                    + " AND btrim(email)<>''",
                Integer.class,
                recipientUserId);
        if (valid == null || valid != 1)
          throw new BizException(ResultEnum.PARAM_ERROR, "邮件接收用户不存在、已停用或未配置邮箱");
        jdbcTemplate.update(
            "INSERT INTO t_sys_monitor_alert_rule_recipient(id,rule_id,user_id,create_time)"
                + " VALUES(?,?,?,?)",
            IdWorker.getId(),
            form.id(),
            recipientUserId,
            OffsetDateTime.now(ZoneOffset.UTC));
      }
    }
    if (Boolean.TRUE.equals(form.emailEnabled())
        && (form.recipientUserIds() == null || form.recipientUserIds().isEmpty())) {
      throw new BizException(ResultEnum.PARAM_ERROR, "启用邮件通知时必须配置接收人");
    }
  }

  private void validateThreshold(Map<String, Object> metadata, MonitorAlertRuleSaveForm form) {
    java.math.BigDecimal minimum = new java.math.BigDecimal(metadata.get("min_value").toString());
    java.math.BigDecimal maximum =
        metadata.get("max_value") == null
            ? null
            : new java.math.BigDecimal(metadata.get("max_value").toString());
    if (form.threshold().compareTo(minimum) < 0
        || maximum != null && form.threshold().compareTo(maximum) > 0) {
      throw new BizException(ResultEnum.PARAM_ERROR, "触发阈值超出该规则允许范围");
    }
    if ("BOOLEAN".equals(metadata.get("value_kind"))
        && (form.threshold().compareTo(java.math.BigDecimal.ONE) != 0
            || form.recoveryThreshold() == null
            || form.recoveryThreshold().compareTo(java.math.BigDecimal.ZERO) != 0)) {
      throw new BizException(ResultEnum.PARAM_ERROR, "状态型规则必须使用触发值 1 和恢复值 0");
    }
  }

  private String trim(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
