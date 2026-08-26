package sm.domain.sys.monitor.runtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
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

  public HostSnapshotVO host(String hostId) {
    return read("sm:monitor:snapshot:host:" + hostId, HostSnapshotVO.class);
  }

  public InstanceSnapshotVO instance(String instanceId) {
    String resolved = registry.require(instanceId).getInstanceId();
    return read("sm:monitor:snapshot:instance:" + resolved, InstanceSnapshotVO.class);
  }

  public HostSnapshotVO currentHost() {
    var pair = store.current();
    return pair == null ? null : pair.host();
  }

  public InstanceSnapshotVO currentInstance() {
    var pair = store.current();
    return pair == null ? null : pair.instance();
  }

  public boolean hasHostSnapshot(String hostId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey("sm:monitor:snapshot:host:" + hostId));
  }

  private <T> T read(String key, Class<T> type) {
    try {
      String json = redisTemplate.opsForValue().get(key);
      if (json == null) throw new BizException(ResultEnum.NOT_FOUND, "目标当前遥测快照不可用");
      return jsonMapper.readValue(json, type);
    } catch (BizException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new BizException(ResultEnum.PERSISTENCE_ERROR, "目标当前遥测快照损坏", exception);
    }
  }
}
