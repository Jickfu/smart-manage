package sm.domain.sys.monitor.runtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.runtime.model.vo.*;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class MonitorSnapshotService {
  private final MonitorInstanceRegistry registry;
  private final StringRedisTemplate redisTemplate;
  private final JsonMapper jsonMapper;
  private final MonitorSnapshotStore store;
  private final JdbcTemplate jdbcTemplate;

  public MonitorCurrentTelemetryVO<HostSnapshotVO> host(String hostId) {
    requireCatalogObject("t_sys_monitor_host", "host_id", hostId, "主机不存在");
    HostSnapshotVO snapshot = find("sm:monitor:snapshot:host:" + hostId, HostSnapshotVO.class);
    return snapshot == null
        ? MonitorCurrentTelemetryVO.unavailable()
        : MonitorCurrentTelemetryVO.available(snapshot);
  }

  public MonitorCurrentTelemetryVO<InstanceSnapshotVO> instance(String instanceId) {
    String resolved =
        instanceId == null || instanceId.isBlank()
            ? registry.currentInstanceId()
            : instanceId.trim();
    requireCatalogObject("t_sys_monitor_instance", "instance_id", resolved, "实例不存在");
    InstanceSnapshotVO snapshot =
        find("sm:monitor:snapshot:instance:" + resolved, InstanceSnapshotVO.class);
    return snapshot == null
        ? MonitorCurrentTelemetryVO.unavailable()
        : MonitorCurrentTelemetryVO.available(snapshot);
  }

  public HostSnapshotVO currentHost() {
    return store.currentHost();
  }

  public InstanceSnapshotVO currentInstance() {
    return store.currentInstance();
  }

  public boolean hasHostSnapshot(String hostId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey("sm:monitor:snapshot:host:" + hostId));
  }

  private <T> T find(String key, Class<T> type) {
    try {
      String json = redisTemplate.opsForValue().get(key);
      return json == null ? null : jsonMapper.readValue(json, type);
    } catch (Exception exception) {
      throw new BizException(ResultEnum.PERSISTENCE_ERROR, "目标当前遥测快照损坏", exception);
    }
  }

  private void requireCatalogObject(String table, String idColumn, String id, String message) {
    if (id == null || id.isBlank()) throw new BizException(ResultEnum.PARAM_ERROR, message);
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM " + table + " WHERE " + idColumn + "=?", Integer.class, id);
    if (count == null || count != 1) throw new BizException(ResultEnum.NOT_FOUND, message);
  }
}
