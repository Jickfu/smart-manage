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

  public void retire(String instanceId) {
    if (instanceId == null || instanceId.isBlank())
      throw new sm.system.exception.BizException(
          sm.system.response.ResultEnum.PARAM_ERROR, "实例 ID 不能为空");
    lifecycleTxService.retire(instanceId.trim());
  }

  public List<MonitorTopologyVO> topology() {
    Map<String, MonitorInstanceVO> online = new HashMap<>();
    for (var item : registry.listOnline()) online.put(item.getInstanceId(), item);
    List<MonitorTopologyVO> result =
        jdbcTemplate.query(
            "SELECT host_id,host_name,os_name,os_version FROM t_sys_monitor_host ORDER BY"
                + " host_name,host_id",
            (hostRs, row) -> host(hostRs));
    for (MonitorTopologyVO host : result) {
      List<MonitorTopologyVO.Instance> instances =
          jdbcTemplate.query(
              "SELECT"
                  + " instance_id,application_name,application_version,lifecycle,last_seen_time,retired_at"
                  + " FROM t_sys_monitor_instance WHERE host_id=? ORDER BY instance_id",
              (rs, row) -> instance(rs, online),
              host.getHostId());
      host.setInstances(instances);
      host.setTelemetryStatus(
          snapshotService.hasHostSnapshot(host.getHostId()) ? "UP" : "TELEMETRY_UNAVAILABLE");
    }
    return result;
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
