package sm.domain.sys.monitor.home.service;

import java.sql.ResultSet;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.home.model.vo.MonitorOverviewVO;
import sm.domain.sys.monitor.runtime.service.*;

@Service
@RequiredArgsConstructor
public class MonitorOverviewService {
  private final MonitorTopologyService topologyService;
  private final MonitorSnapshotService snapshotService;
  private final JdbcTemplate jdbcTemplate;

  public MonitorOverviewVO overview() {
    var topology = topologyService.topology();
    var result = new MonitorOverviewVO();
    result.setHostTotal(topology.size());
    result.setHostTelemetryAvailable(
        (int) topology.stream().filter(item -> "UP".equals(item.getTelemetryStatus())).count());
    result.setApplicationTotal(
        topology.stream()
            .mapToInt(
                item ->
                    (int)
                        item.getInstances().stream()
                            .filter(instance -> "ACTIVE".equals(instance.getLifecycle()))
                            .count())
            .sum());
    result.setApplicationOnline(
        topology.stream()
            .mapToInt(
                item ->
                    (int)
                        item.getInstances().stream()
                            .filter(
                                instance ->
                                    instance.isOnline() && "ACTIVE".equals(instance.getLifecycle()))
                            .count())
            .sum());
    var snapshot = snapshotService.currentInstance();
    result.setDatabaseHealth(snapshot == null ? "UNKNOWN" : health(snapshot, "db"));
    result.setRedisHealth(snapshot == null ? "UNKNOWN" : health(snapshot, "redis"));
    Map<String, Object> counts =
        jdbcTemplate.queryForMap(
            "SELECT count(*) FILTER(WHERE status='PENDING') pending_count,count(*) FILTER(WHERE"
                + " status='FIRING') firing_count,count(*) FILTER(WHERE status='FIRING' AND"
                + " severity='CRITICAL') critical_count FROM t_sys_monitor_alert_incident a JOIN"
                + " t_sys_monitor_alert_rule b ON b.id=a.rule_id WHERE a.status IN"
                + " ('PENDING','FIRING')");
    result.setPendingCount(((Number) counts.get("pending_count")).intValue());
    result.setFiringCount(((Number) counts.get("firing_count")).intValue());
    result.setCriticalCount(((Number) counts.get("critical_count")).intValue());
    result.setCurrentAbnormal(
        jdbcTemplate.query(
            "SELECT b.severity,a.rule_code,a.scope_type,a.scope_id,a.summary FROM"
                + " t_sys_monitor_alert_incident a JOIN t_sys_monitor_alert_rule b ON"
                + " b.id=a.rule_id WHERE a.status IN ('PENDING','FIRING') ORDER BY CASE b.severity"
                + " WHEN 'CRITICAL' THEN 1 WHEN 'WARNING' THEN 2 ELSE 3 END,a.started_at LIMIT 10",
            (rs, row) -> attention(rs)));
    List<MonitorOverviewVO.HostSummary> summaries = new ArrayList<>();
    for (var host : topology) {
      var item = new MonitorOverviewVO.HostSummary();
      item.setHostId(host.getHostId());
      item.setHostName(host.getHostName());
      item.setTelemetryStatus(host.getTelemetryStatus());
      item.setTotalInstances(host.getInstances().size());
      item.setOnlineInstances(
          (int) host.getInstances().stream().filter(instance -> instance.isOnline()).count());
      summaries.add(item);
    }
    result.setTopology(summaries);
    return result;
  }

  private String health(
      sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO snapshot, String name) {
    return snapshot.getHealth().getComponents().stream()
        .filter(item -> item.getName().equalsIgnoreCase(name))
        .map(sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO.HealthComponent::getStatus)
        .findFirst()
        .orElse("UNKNOWN");
  }

  private MonitorOverviewVO.AttentionItem attention(ResultSet rs) throws java.sql.SQLException {
    var item = new MonitorOverviewVO.AttentionItem();
    item.setSeverity(rs.getString("severity"));
    item.setRuleCode(rs.getString("rule_code"));
    item.setScopeType(rs.getString("scope_type"));
    item.setScopeId(rs.getString("scope_id"));
    item.setSummary(rs.getString("summary"));
    return item;
  }
}
