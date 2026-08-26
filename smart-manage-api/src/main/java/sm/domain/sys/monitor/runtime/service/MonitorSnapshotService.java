package sm.domain.sys.monitor.runtime.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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
    HostSnapshotVO snapshot = currentCanonicalHost(hostId);
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

  /** 按 metric 从同一 Host 的新鲜实例观测中选取最新有效值，供当前遥测和 Host 告警共同使用。 */
  public HostSnapshotVO currentCanonicalHost(String hostId) {
    if (hostId == null || hostId.isBlank()) return null;
    String normalizedHostId = hostId.trim();
    try {
      List<HostObservationSourceVO> sources = freshHostSources(normalizedHostId);
      return sources.isEmpty() ? null : assembleCanonicalHost(normalizedHostId, sources);
    } catch (Exception exception) {
      // Redis 断连时 Instance 规则仍需继续评估，因此 Host source 读取失败仅令 Host 指标未知。
      log.warn("读取 Host 观测源失败：hostId={}", normalizedHostId, exception);
      return null;
    }
  }

  public InstanceSnapshotVO currentInstance() {
    return store.currentInstance();
  }

  public boolean hasHostSnapshot(String hostId) {
    return currentCanonicalHost(hostId) != null;
  }

  static String hostSourceKey(String hostId, String instanceId) {
    return "sm:monitor:snapshot:host-source:" + hostId + ":" + instanceId;
  }

  static String hostSourceIndexKey(String hostId) {
    return "sm:monitor:snapshot:host-sources:" + hostId;
  }

  private List<HostObservationSourceVO> freshHostSources(String hostId) throws Exception {
    long ttlMillis = properties.getSampling().getSnapshotTtlSeconds() * 1000L;
    long cutoff = System.currentTimeMillis() - ttlMillis;
    Set<String> instanceIds =
        redisTemplate
            .opsForZSet()
            .rangeByScore(hostSourceIndexKey(hostId), cutoff, Double.MAX_VALUE);
    if (instanceIds == null || instanceIds.isEmpty()) return List.of();
    List<String> sourceInstanceIds = new ArrayList<>(instanceIds);
    List<String> keys = new ArrayList<>(sourceInstanceIds.size());
    for (String instanceId : sourceInstanceIds) keys.add(hostSourceKey(hostId, instanceId));
    List<String> jsonValues = redisTemplate.opsForValue().multiGet(keys);
    if (jsonValues == null) return List.of();
    List<HostObservationSourceVO> result = new ArrayList<>();
    int valueCount = Math.min(keys.size(), jsonValues.size());
    for (int index = 0; index < valueCount; index++) {
      String json = jsonValues.get(index);
      if (json == null) continue;
      String expectedInstanceId = sourceInstanceIds.get(index);
      try {
        HostObservationSourceVO source = jsonMapper.readValue(json, HostObservationSourceVO.class);
        if (validSource(hostId, expectedInstanceId, source)) result.add(source);
      } catch (Exception exception) {
        log.warn(
            "忽略损坏的 Host 观测源：hostId={}，instanceId={}",
            hostId,
            expectedInstanceId,
            exception);
      }
    }
    return result;
  }

  private boolean validSource(
      String hostId, String expectedInstanceId, HostObservationSourceVO source) {
    HostSnapshotVO snapshot = source == null ? null : source.getSnapshot();
    Instant sampleTime = snapshot == null ? null : snapshot.getSampleTime();
    boolean identityMatches =
        source != null
            && snapshot != null
            && hostId.equals(source.getHostId())
            && expectedInstanceId.equals(source.getInstanceId())
            && hostId.equals(snapshot.getHostId());
    boolean fresh =
        sampleTime != null
            && !Instant.now()
                .isAfter(
                    sampleTime.plusSeconds(properties.getSampling().getSnapshotTtlSeconds()));
    if (!identityMatches || !fresh) {
      log.warn(
          "忽略身份不匹配或过期的 Host 观测源：hostId={}，instanceId={}", hostId, expectedInstanceId);
    }
    return identityMatches && fresh;
  }

  private HostSnapshotVO assembleCanonicalHost(
      String hostId, List<HostObservationSourceVO> sources) {
    HostSnapshotVO base = newest(sources, snapshot -> true).getSnapshot();
    HostSnapshotVO result = new HostSnapshotVO();
    result.setHostId(hostId);
    result.setHostname(base.getHostname());
    result.setSampleTime(base.getSampleTime());
    result.setUptimeMs(base.getUptimeMs());
    result.setOs(base.getOs());

    HostSnapshotVO.CpuInfo cpu = new HostSnapshotVO.CpuInfo();
    HostObservationSourceVO cpuUsage =
        newest(sources, snapshot -> snapshot.getCpu() != null && snapshot.getCpu().getUsage() != null);
    HostObservationSourceVO loadAverage =
        newest(
            sources,
            snapshot -> snapshot.getCpu() != null && snapshot.getCpu().getLoadAverage() != null);
    cpu.setUsage(cpuUsage == null ? null : cpuUsage.getSnapshot().getCpu().getUsage());
    cpu.setLoadAverage(
        loadAverage == null ? null : loadAverage.getSnapshot().getCpu().getLoadAverage());
    result.setCpu(cpu);

    HostSnapshotVO.MemoryInfo memory = new HostSnapshotVO.MemoryInfo();
    HostObservationSourceVO memoryUsage =
        newest(
            sources,
            snapshot ->
                snapshot.getMemory() != null
                    && snapshot.getMemory().isCollectorAvailable()
                    && snapshot.getMemory().getTotal() > 0);
    HostObservationSourceVO swapUsage =
        newest(
            sources,
            snapshot ->
                snapshot.getMemory() != null
                    && snapshot.getMemory().isCollectorAvailable()
                    && snapshot.getMemory().getSwapTotal() > 0);
    if (memoryUsage != null) {
      HostSnapshotVO.MemoryInfo selected = memoryUsage.getSnapshot().getMemory();
      memory.setTotal(selected.getTotal());
      memory.setAvailable(selected.getAvailable());
    }
    if (swapUsage != null) {
      HostSnapshotVO.MemoryInfo selected = swapUsage.getSnapshot().getMemory();
      memory.setSwapTotal(selected.getSwapTotal());
      memory.setSwapUsed(selected.getSwapUsed());
    }
    memory.setCollectorAvailable(memoryUsage != null || swapUsage != null);
    result.setMemory(memory);

    HostObservationSourceVO filesystems =
        newest(
            sources,
            snapshot ->
                snapshot.isFilesystemsAvailable()
                    && snapshot.getFilesystems() != null
                    && snapshot.getFilesystems().stream()
                        .anyMatch(filesystem -> filesystem.getUsage() != null));
    result.setFilesystemsAvailable(filesystems != null);
    result.setFilesystems(
        filesystems == null ? List.of() : filesystems.getSnapshot().getFilesystems());

    HostObservationSourceVO io =
        newest(
            sources,
            snapshot -> snapshot.getIo() != null && snapshot.getIo().isCollectorAvailable());
    result.setIo(io == null ? new HostSnapshotVO.IoInfo() : io.getSnapshot().getIo());
    return result;
  }

  private HostObservationSourceVO newest(
      List<HostObservationSourceVO> sources, Predicate<HostSnapshotVO> validMetric) {
    return sources.stream()
        .filter(source -> validMetric.test(source.getSnapshot()))
        .max(Comparator.comparing(source -> source.getSnapshot().getSampleTime()))
        .orElse(null);
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
