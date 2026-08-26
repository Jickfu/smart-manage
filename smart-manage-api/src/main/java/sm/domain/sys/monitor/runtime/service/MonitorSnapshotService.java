package sm.domain.sys.monitor.runtime.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.runtime.model.vo.*;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorSnapshotService {
  private final MonitorInstanceRegistry registry;
  private final StringRedisTemplate redisTemplate;
  private final JsonMapper jsonMapper;
  private final MonitorSnapshotStore store;
  private final JdbcTemplate jdbcTemplate;
  private final MonitorProperties properties;

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

  /**
   * 告警引擎使用的 Host 权威观测。同一 Host 上的所有 JVM 必须读取同一个 Redis 快照，避免各自的
   * 本地 OSHI 采样把 Host 规则评估成不同事实。
   */
  public HostSnapshotVO currentCanonicalHost(String hostId) {
    if (hostId == null || hostId.isBlank()) return null;
    String normalizedHostId = hostId.trim();
    try {
      HostSnapshotVO snapshot =
          find("sm:monitor:snapshot:host:" + normalizedHostId, HostSnapshotVO.class);
      if (snapshot == null) return null;
      if (!normalizedHostId.equals(snapshot.getHostId())) {
        log.warn(
            "忽略 Host ID 不匹配的权威监控快照：keyHostId={}，snapshotHostId={}",
            normalizedHostId,
            snapshot.getHostId());
        return null;
      }
      Instant sampleTime = snapshot.getSampleTime();
      long ttlSeconds = properties.getSampling().getSnapshotTtlSeconds();
      return sampleTime != null && !Instant.now().isAfter(sampleTime.plusSeconds(ttlSeconds))
          ? snapshot
          : null;
    } catch (BizException exception) {
      // 告警调度不能因一条损坏快照中断全部规则；损坏数据按当前指标未知处理并保留可诊断日志。
      log.warn("忽略损坏的 Host 权威监控快照：hostId={}", normalizedHostId, exception);
      return null;
    }
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
