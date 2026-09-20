package sm.domain.sys.monitor.runtime.service;

import java.sql.ResultSet;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.runtime.model.vo.MonitorTopologyVO;

@Service
@RequiredArgsConstructor
public class MonitorTopologyService {
  private final JdbcTemplate jdbcTemplate;
  private final MonitorInstanceRegistry registry;
  private final MonitorSnapshotService snapshotService;
  private final MonitorInstanceLifecycleTxService lifecycleTxService;

  public List<MonitorInstanceVO> onlineInstances() {
    return registry.listOnline();
  }

  public List<MonitorInstanceVO> catalogInstances() {
    Map<String, MonitorInstanceVO> online = onlineIndex();
    return jdbcTemplate.query(
        """
SELECT instance_id,host_id,application_name,application_version,lifecycle,last_start_time,last_seen_time
FROM t_sys_monitor_instance ORDER BY instance_id
""",
        (rs, row) -> {
          MonitorInstanceVO live = online.get(rs.getString("instance_id"));
          MonitorInstanceVO value = live == null ? new MonitorInstanceVO() : live;
          value.setInstanceId(rs.getString("instance_id"));
          value.setHostId(rs.getString("host_id"));
          value.setApplicationName(rs.getString("application_name"));
          value.setApplicationVersion(rs.getString("application_version"));
          value.setLifecycle(rs.getString("lifecycle"));
          value.setOnline(live != null);
          value.setCurrent(registry.isCurrent(value.getInstanceId()));
          if (live == null) {
            value.setStartTime(
                toIso(rs.getObject("last_start_time", java.time.OffsetDateTime.class)));
            value.setLastSeenTime(
                toIso(rs.getObject("last_seen_time", java.time.OffsetDateTime.class)));
          }
          return value;
        });
  }

  public void retire(String instanceId) {
    if (instanceId == null || instanceId.isBlank())
      throw new sm.system.exception.BizException(
          sm.system.response.ResultEnum.PARAM_ERROR, "实例 ID 不能为空");
    String targetInstanceId = instanceId.trim();
    if (registry.listOnline().stream()
        .anyMatch(instance -> targetInstanceId.equals(instance.getInstanceId()))) {
      throw new sm.system.exception.BizException(
          sm.system.response.ResultEnum.DATA_CONFLICT, "在线实例不能退役，请先停止该实例");
    }
    lifecycleTxService.retire(targetInstanceId);
  }

  public List<MonitorTopologyVO> topology() {
    Map<String, MonitorInstanceVO> online = onlineIndex();
    List<MonitorTopologyVO> result =
        jdbcTemplate.query(
            """
            SELECT host_id,host_name,os_name,os_version
            FROM t_sys_monitor_host host
            WHERE EXISTS (SELECT 1 FROM t_sys_monitor_instance instance
                          WHERE instance.host_id=host.host_id AND instance.lifecycle='ACTIVE')
            ORDER BY host_name,host_id
            """,
            (hostRs, row) -> host(hostRs));
    for (MonitorTopologyVO host : result) {
      List<MonitorTopologyVO.Instance> instances =
          jdbcTemplate.query(
              """
SELECT instance_id,application_name,application_version,lifecycle,last_seen_time,retired_at
FROM t_sys_monitor_instance WHERE host_id=? AND lifecycle='ACTIVE' ORDER BY instance_id
""",
              (rs, row) -> instance(rs, online),
              host.getHostId());
      host.setInstances(instances);
      host.setTelemetryStatus(
          snapshotService.hasHostSnapshot(host.getHostId()) ? "UP" : "TELEMETRY_UNAVAILABLE");
    }
    return result;
  }

  private Map<String, MonitorInstanceVO> onlineIndex() {
    Map<String, MonitorInstanceVO> online = new HashMap<>();
    for (MonitorInstanceVO item : registry.listOnline()) online.put(item.getInstanceId(), item);
    return online;
  }

  private String toIso(java.time.OffsetDateTime value) {
    return value == null ? null : value.toInstant().toString();
  }

  private MonitorTopologyVO host(ResultSet rs) throws java.sql.SQLException {
    var value = new MonitorTopologyVO();
    value.setHostId(rs.getString("host_id"));
    value.setHostName(rs.getString("host_name"));
    value.setOsName(rs.getString("os_name"));
    value.setOsVersion(rs.getString("os_version"));
    return value;
  }

  private MonitorTopologyVO.Instance instance(ResultSet rs, Map<String, MonitorInstanceVO> online)
      throws java.sql.SQLException {
    var value = new MonitorTopologyVO.Instance();
    value.setInstanceId(rs.getString("instance_id"));
    value.setApplicationName(rs.getString("application_name"));
    value.setApplicationVersion(rs.getString("application_version"));
    value.setLifecycle(rs.getString("lifecycle"));
    value.setLastSeenTime(rs.getObject("last_seen_time", java.time.OffsetDateTime.class));
    value.setRetiredAt(rs.getObject("retired_at", java.time.OffsetDateTime.class));
    MonitorInstanceVO live = online.get(value.getInstanceId());
    value.setOnline(live != null);
    value.setCurrent(live != null && live.isCurrent());
    return value;
  }
}
