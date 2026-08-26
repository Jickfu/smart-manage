package sm.domain.sys.monitor.alert.service;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.monitor.alert.model.form.*;
import sm.domain.sys.monitor.alert.model.vo.*;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.security.context.CurrentUserContext;

@Service
@RequiredArgsConstructor
public class MonitorAlertService {
  private final JdbcTemplate jdbcTemplate;
  private final MonitorAlertTxService txService;
  private final MonitorAlertStateTxService stateTxService;
  private final CurrentUserContext currentUserContext;
  private final MonitorMetricValueFormatter valueFormatter;

  public List<MonitorAlertRuleVO> rules() {
    return jdbcTemplate.query(
        "SELECT * FROM t_sys_monitor_alert_rule ORDER BY id",
        (rs, row) ->
            new MonitorAlertRuleVO(
                rs.getLong("id"),
                rs.getString("rule_code"),
                rs.getString("name"),
                rs.getString("scope_type"),
                rs.getBoolean("enabled"),
                rs.getString("severity"),
                rs.getBigDecimal("threshold"),
                rs.getInt("duration_seconds"),
                rs.getBigDecimal("recovery_threshold"),
                rs.getInt("repeat_interval_seconds"),
                rs.getBoolean("email_enabled"),
                rs.getString("description"),
                rs.getInt("version"),
                rs.getString("value_kind"),
                rs.getString("display_unit"),
                rs.getBigDecimal("min_value"),
                rs.getBigDecimal("max_value"),
                rs.getBigDecimal("recommended_threshold"),
                recipients(rs.getLong("id"))));
  }

  private List<MonitorAlertRuleVO.UserRef> recipients(long ruleId) {
    return jdbcTemplate.query(
        "SELECT b.id,b.number,b.name FROM t_sys_monitor_alert_rule_recipient a JOIN t_sys_user b ON"
            + " b.id=a.user_id WHERE a.rule_id=? ORDER BY b.number,b.id",
        (rs, row) ->
            new MonitorAlertRuleVO.UserRef(
                rs.getLong("id"), rs.getString("number"), rs.getString("name")),
        ruleId);
  }

  @BizLog("保存监控告警规则")
  public void saveRule(MonitorAlertRuleSaveForm form) {
    currentUserContext.checkAdministrator();
    txService.saveRule(form, currentUserContext.getUserId());
  }

  public PageData<MonitorAlertIncidentVO> incidents(MonitorAlertIncidentListForm form) {
    List<Object> arguments = new ArrayList<>();
    StringBuilder where = new StringBuilder(" WHERE 1=1");
    append(where, arguments, "a.status", form.getStatus());
    append(where, arguments, "b.severity", form.getSeverity());
    append(where, arguments, "a.scope_type", form.getScopeType());
    if (StringUtils.hasText(form.getKeyword())) {
      where.append(" AND (a.scope_id ILIKE ? OR a.summary ILIKE ?)");
      arguments.add("%" + form.getKeyword().trim() + "%");
      arguments.add("%" + form.getKeyword().trim() + "%");
    }
    Long total =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_alert_incident a JOIN t_sys_monitor_alert_rule b ON"
                + " b.id=a.rule_id"
                + where,
            Long.class,
            arguments.toArray());
    arguments.add(form.getPageSize());
    arguments.add((form.getPageNum() - 1) * form.getPageSize());
    List<MonitorAlertIncidentVO> records =
        jdbcTemplate.query(
            """
SELECT a.*,b.name rule_name,b.severity,b.value_kind,b.display_unit FROM t_sys_monitor_alert_incident a
JOIN t_sys_monitor_alert_rule b ON b.id=a.rule_id
"""
                + where
                + " ORDER BY a.started_at DESC,a.id DESC LIMIT ? OFFSET ?",
            (rs, row) ->
                new MonitorAlertIncidentVO(
                    rs.getLong("id"),
                    rs.getString("rule_code"),
                    rs.getString("rule_name"),
                    rs.getString("severity"),
                    rs.getString("scope_type"),
                    rs.getString("scope_id"),
                    rs.getString("status"),
                    rs.getString("close_reason"),
                    rs.getObject("started_at", java.time.OffsetDateTime.class),
                    rs.getObject("fired_at", java.time.OffsetDateTime.class),
                    rs.getObject("recovered_at", java.time.OffsetDateTime.class),
                    rs.getBigDecimal("last_value"),
                    rs.getBigDecimal("peak_value"),
                    valueFormatter.format(
                        rs.getBigDecimal("last_value"),
                        rs.getString("value_kind"),
                        rs.getString("display_unit")),
                    valueFormatter.format(
                        rs.getBigDecimal("peak_value"),
                        rs.getString("value_kind"),
                        rs.getString("display_unit")),
                    rs.getString("summary")),
            arguments.toArray());
    return PageData.of(total == null ? 0 : total, form.getPageNum(), form.getPageSize(), records);
  }

  void evaluateInternal(MonitorAlertEvaluation evaluation) {
    stateTxService.evaluate(evaluation);
  }

  private void append(StringBuilder where, List<Object> arguments, String column, String value) {
    if (StringUtils.hasText(value)) {
      where.append(" AND ").append(column).append("=?");
      arguments.add(value.trim());
    }
  }
}
