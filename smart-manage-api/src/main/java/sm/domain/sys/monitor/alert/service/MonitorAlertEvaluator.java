package sm.domain.sys.monitor.alert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.runtime.model.vo.RuntimeSnapshotVO;
import sm.domain.sys.monitor.runtime.service.RuntimeMonitorService;
import java.math.BigDecimal;
import java.util.*;

/** 仅评估预定义规则类型；多实例并发由活动事件唯一约束和事务行锁收敛。 */
@Component
@RequiredArgsConstructor
@Slf4j
class MonitorAlertEvaluator {
    private final JdbcTemplate jdbcTemplate;
    private final RuntimeMonitorService runtimeMonitorService;
    private final MonitorAlertService alertService;


    @Scheduled(fixedDelayString = "${smart-manage.monitor.alert-evaluation-interval-ms:10000}")
    void evaluate() {
        try {
            RuntimeSnapshotVO snapshot = runtimeMonitorService.localSnapshot();
            List<Map<String,Object>> rules = enabledRules();
            for (Map<String,Object> rule : rules) evaluateRule(rule, snapshot);
            rules.stream().filter(rule -> "INSTANCE_OFFLINE".equals(rule.get("rule_code"))).findFirst()
                    .ifPresent(this::evaluateOfflineInstances);
        } catch (Exception exception) {
            log.warn("监控告警评估失败", exception);
        }
    }

    private void evaluateOfflineInstances(Map<String,Object> rule) {
        Set<String> onlineIds = new HashSet<>();
        runtimeMonitorService.instances().forEach(instance -> onlineIds.add(instance.getInstanceId()));
        for (Map<String,Object> instance : jdbcTemplate.queryForList("SELECT instance_id FROM t_sys_monitor_instance")) {
            String instanceId = (String) instance.get("instance_id");
            BigDecimal value = onlineIds.contains(instanceId) ? BigDecimal.ZERO : BigDecimal.ONE;
            BigDecimal threshold = decimal(rule.get("threshold"));
            MonitorAlertEvaluation evaluation = new MonitorAlertEvaluation(((Number) rule.get("id")).longValue(),
                    "INSTANCE_OFFLINE", "INSTANCE", instanceId, value, threshold, value.compareTo(threshold) >= 0,
                    ((Number) rule.get("duration_seconds")).intValue(), decimal(rule.get("recovery_threshold")),
                    ((Number) rule.get("repeat_interval_seconds")).intValue(), Boolean.TRUE.equals(rule.get("email_enabled")),
                    onlineIds.contains(instanceId) ? "应用实例已恢复在线：" + instanceId : "应用实例离线：" + instanceId);
            try { alertService.evaluateInternal(evaluation); } catch (DuplicateKeyException ignored) { }
        }
    }

    private List<Map<String,Object>> enabledRules() {
        return jdbcTemplate.queryForList("SELECT * FROM t_sys_monitor_alert_rule WHERE enabled=true ORDER BY id");
    }

    private void evaluateRule(Map<String,Object> rule, RuntimeSnapshotVO snapshot) {
        String code = (String) rule.get("rule_code");
        String scopeType = (String) rule.get("scope_type");
        if ("INSTANCE_OFFLINE".equals(code)) return; // 由在线注册 TTL 和持久化目录的独立检查处理。
        Double metric = metric(code, snapshot);
        if (metric == null) return;
        BigDecimal value = BigDecimal.valueOf(metric);
        BigDecimal threshold = decimal(rule.get("threshold"));
        BigDecimal recovery = decimal(rule.get("recovery_threshold"));
        boolean violation = value.compareTo(threshold) >= 0;
        String scopeId = "HOST".equals(scopeType) ? snapshot.getHostId() : snapshot.getInstanceId();
        MonitorAlertEvaluation evaluation = new MonitorAlertEvaluation(((Number) rule.get("id")).longValue(), code,
                scopeType, scopeId, value, threshold, violation, ((Number) rule.get("duration_seconds")).intValue(),
                recovery, ((Number) rule.get("repeat_interval_seconds")).intValue(),
                Boolean.TRUE.equals(rule.get("email_enabled")), summary((String) rule.get("name"), scopeId, value, threshold));
        try { alertService.evaluateInternal(evaluation); }
        catch (DuplicateKeyException ignored) { /* 另一实例已创建同一活动事件，下次采样继续评估。 */ }
    }

    private Double metric(String code, RuntimeSnapshotVO snapshot) {
        return switch (code) {
            case "HOST_CPU_HIGH" -> snapshot.getCpu().getSystemUsage();
            case "HOST_MEMORY_HIGH" -> ratio(snapshot.getMemory().getPhysicalTotal() - snapshot.getMemory().getPhysicalAvailable(), snapshot.getMemory().getPhysicalTotal());
            case "HOST_SWAP_HIGH" -> ratio(snapshot.getMemory().getSwapUsed(), snapshot.getMemory().getSwapTotal());
            case "HOST_DISK_HIGH" -> snapshot.getFilesystems().stream().mapToDouble(item -> ratio(item.getUsed(), item.getTotal())).max().orElse(0);
            case "INSTANCE_HEAP_HIGH" -> ratio(snapshot.getMemory().getHeapUsed(), snapshot.getMemory().getHeapMax());
            case "INSTANCE_BLOCKED_THREADS" -> snapshot.getThreads().getStateCounts().getOrDefault("BLOCKED", 0).doubleValue();
            case "HTTP_ERROR_RATE_HIGH" -> snapshot.getHttp().getServerErrorRate();
            case "HTTP_LATENCY_HIGH" -> snapshot.getHttp().getP95Ms();
            case "DB_HEALTH_DOWN" -> healthDown(snapshot, "db");
            case "DB_POOL_HIGH" -> ratio(snapshot.getDataSource().getActive(), snapshot.getDataSource().getMaxActive());
            case "DB_POOL_WAITING" -> (double) snapshot.getDataSource().getWaiting();
            case "REDIS_HEALTH_DOWN" -> healthDown(snapshot, "redis");
            default -> null;
        };
    }

    private double healthDown(RuntimeSnapshotVO snapshot, String name) {
        return snapshot.getHealth().getComponents().stream().filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst().map(item -> "UP".equals(item.getStatus()) ? 0d : 1d).orElse(1d);
    }
    private double ratio(long used, long total) { return total > 0 ? (double) used / total : 0; }
    private BigDecimal decimal(Object value) { return value == null ? null : new BigDecimal(value.toString()); }
    private String summary(String name, String scopeId, BigDecimal value, BigDecimal threshold) {
        return name + "：" + scopeId + " 当前值 " + value.stripTrailingZeros().toPlainString()
                + "，阈值 " + threshold.stripTrailingZeros().toPlainString();
    }
}
