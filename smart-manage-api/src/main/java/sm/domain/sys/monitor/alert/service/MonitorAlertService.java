package sm.domain.sys.monitor.alert.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.monitor.alert.model.form.*;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.security.context.CurrentUserContext;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MonitorAlertService {
    private final JdbcTemplate jdbcTemplate;
    private final MonitorAlertTxService txService;
    private final MonitorAlertStateTxService stateTxService;
    private final CurrentUserContext currentUserContext;

    public List<Map<String,Object>> rules() {
        List<Map<String,Object>> rules = jdbcTemplate.queryForList("SELECT * FROM t_sys_monitor_alert_rule ORDER BY id");
        for (Map<String,Object> rule : rules) {
            rule.put("recipient_users", jdbcTemplate.queryForList("""
                    SELECT b.id,b.number,b.name FROM t_sys_monitor_alert_rule_recipient a
                    JOIN t_sys_user b ON b.id=a.user_id WHERE a.rule_id=? ORDER BY b.number,b.id
                    """, rule.get("id")));
        }
        return rules;
    }

    @BizLog("保存监控告警规则")
    public void saveRule(MonitorAlertRuleSaveForm form) {
        currentUserContext.checkAdministrator();
        txService.saveRule(form, currentUserContext.getUserId());
    }

    public PageData<Map<String,Object>> incidents(MonitorAlertIncidentListForm form) {
        List<Object> arguments = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        append(where, arguments, "a.status", form.getStatus());
        append(where, arguments, "b.severity", form.getSeverity());
        append(where, arguments, "a.scope_type", form.getScopeType());
        if (StringUtils.hasText(form.getKeyword())) {
            where.append(" AND (a.scope_id ILIKE ? OR a.summary ILIKE ?)");
            arguments.add("%" + form.getKeyword().trim() + "%"); arguments.add("%" + form.getKeyword().trim() + "%");
        }
        Long total = jdbcTemplate.queryForObject("SELECT count(*) FROM t_sys_monitor_alert_incident a JOIN t_sys_monitor_alert_rule b ON b.id=a.rule_id" + where,
                Long.class, arguments.toArray());
        arguments.add(form.getPageSize()); arguments.add((form.getPageNum() - 1) * form.getPageSize());
        List<Map<String,Object>> records = jdbcTemplate.queryForList("""
                SELECT a.*,b.name rule_name,b.severity FROM t_sys_monitor_alert_incident a
                JOIN t_sys_monitor_alert_rule b ON b.id=a.rule_id
                """ + where + " ORDER BY a.started_at DESC,a.id DESC LIMIT ? OFFSET ?", arguments.toArray());
        return PageData.of(total == null ? 0 : total, form.getPageNum(), form.getPageSize(), records);
    }

    void evaluateInternal(MonitorAlertEvaluation evaluation) {
        stateTxService.evaluate(evaluation);
    }

    private void append(StringBuilder where, List<Object> arguments, String column, String value) {
        if (StringUtils.hasText(value)) { where.append(" AND ").append(column).append("=?"); arguments.add(value.trim()); }
    }
}
