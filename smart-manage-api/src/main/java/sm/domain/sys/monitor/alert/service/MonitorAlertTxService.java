package sm.domain.sys.monitor.alert.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.monitor.alert.model.form.MonitorAlertRuleSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class MonitorAlertTxService {
    private static final Set<String> SEVERITIES = Set.of("INFO", "WARNING", "CRITICAL");
    private final JdbcTemplate jdbcTemplate;

    void saveRule(MonitorAlertRuleSaveForm form, Long userId) {
        if (!SEVERITIES.contains(form.severity())) throw new BizException(ResultEnum.PARAM_ERROR, "告警严重程度不合法");
        if (form.recoveryThreshold() != null && form.recoveryThreshold().compareTo(form.threshold()) > 0) {
            throw new BizException(ResultEnum.PARAM_ERROR, "恢复阈值不能高于触发阈值");
        }
        int changed = jdbcTemplate.update("""
                UPDATE t_sys_monitor_alert_rule SET enabled=?,severity=?,threshold=?,duration_seconds=?,
                recovery_threshold=?,repeat_interval_seconds=?,email_enabled=?,description=?,update_time=?,update_user=?,version=version+1
                WHERE id=? AND version=?
                """, form.enabled(), form.severity(), form.threshold(), form.durationSeconds(), form.recoveryThreshold(),
                form.repeatIntervalSeconds(), form.emailEnabled(), trim(form.description()), OffsetDateTime.now(ZoneOffset.UTC), userId, form.id(), form.version());
        if (changed != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "告警规则不存在或已被其他用户修改");
        jdbcTemplate.update("DELETE FROM t_sys_monitor_alert_rule_recipient WHERE rule_id=?", form.id());
        if (form.recipientUserIds() != null) {
            for (Long recipientUserId : new LinkedHashSet<>(form.recipientUserIds())) {
                Integer valid = jdbcTemplate.queryForObject("SELECT count(*) FROM t_sys_user WHERE id=? AND enabled=true AND email IS NOT NULL AND btrim(email)<>''", Integer.class, recipientUserId);
                if (valid == null || valid != 1) throw new BizException(ResultEnum.PARAM_ERROR, "邮件接收用户不存在、已停用或未配置邮箱");
                jdbcTemplate.update("INSERT INTO t_sys_monitor_alert_rule_recipient(id,rule_id,user_id,create_time) VALUES(?,?,?,?)",
                        IdWorker.getId(), form.id(), recipientUserId, OffsetDateTime.now(ZoneOffset.UTC));
            }
        }
        if (Boolean.TRUE.equals(form.emailEnabled()) && (form.recipientUserIds() == null || form.recipientUserIds().isEmpty())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "启用邮件通知时必须配置接收人");
        }
    }

    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
