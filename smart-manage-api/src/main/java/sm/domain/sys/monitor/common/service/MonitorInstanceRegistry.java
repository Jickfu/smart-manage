package sm.domain.sys.monitor.common.service;

import jakarta.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.json.JsonMapper;

/** Redis 在线实例注册表；实例详情独立 TTL，索引只用于发现候选实例。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonitorInstanceRegistry {
  private static final String INDEX_KEY = "sm:monitor:instances";
  private static final String INSTANCE_KEY_PREFIX = "sm:monitor:instance:";
  private static final String OWNER_KEY_PREFIX = "sm:monitor:instance-owner:";
  private static final DefaultRedisScript<Long> CLAIM_SCRIPT =
      new DefaultRedisScript<>(
          """
          local owner = redis.call('GET', KEYS[2])
          if (not owner) or owner == ARGV[1] then
            redis.call('PSETEX', KEYS[1], ARGV[3], ARGV[2])
            redis.call('PSETEX', KEYS[2], ARGV[3], ARGV[1])
            return 1
          end
          return 0
          """,
          Long.class);
  private static final DefaultRedisScript<Long> RELEASE_SCRIPT =
      new DefaultRedisScript<>(
          """
if redis.call('GET', KEYS[2]) == ARGV[1] then redis.call('DEL', KEYS[1], KEYS[2]); return 1 end
return 0
""",
          Long.class);

  private final StringRedisTemplate redisTemplate;
  private final JsonMapper jsonMapper;
  private final MonitorProperties properties;
  private final MonitorCatalogAccessor catalogAccessor;
  private final SystemInfo systemInfo = new SystemInfo();
  private final String registrationToken = UUID.randomUUID().toString();
  private volatile long lastCatalogRefreshTime;

  @Value("${smart-manage.instance-id}")
  private String instanceId;

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${spring.application.version:unknown}")
  private String applicationVersion;

  @EventListener(ApplicationReadyEvent.class)
  public void registerWhenReady() {
    // Redis 是系统基础设施，启动阶段注册失败必须中止启动，禁止伪装成可用实例。
    heartbeat(true);
  }

  @Scheduled(fixedDelayString = "${smart-manage.monitor.cluster.heartbeat-interval-ms:10000}")
  public void scheduledHeartbeat() {
    // 运行中断连时保留进程以便健康检查、告警和连接池自动恢复，但心跳失败不得吞掉。
    heartbeat(false);
  }

  public void heartbeat() {
    heartbeat(false);
  }

  private void heartbeat(boolean forceCatalogRefresh) {
    MonitorProperties.Cluster cluster = properties.getCluster();
    validateInternalBaseUrl(cluster.getInternalBaseUrl());
    long now = System.currentTimeMillis();
    RegisteredInstance registeredInstance = new RegisteredInstance();
    registeredInstance.setRegistrationToken(registrationToken);
    registeredInstance.setInstanceId(instanceId);
    registeredInstance.setHostId(resolveHostId());
    registeredInstance.setHostName(
        systemInfo.getOperatingSystem().getNetworkParams().getHostName());
    registeredInstance.setOsName(systemInfo.getOperatingSystem().getFamily());
    registeredInstance.setOsVersion(systemInfo.getOperatingSystem().getVersionInfo().getVersion());
    registeredInstance.setArch(System.getProperty("os.arch"));
    registeredInstance.setApplicationName(applicationName);
    registeredInstance.setApplicationVersion(applicationVersion);
    registeredInstance.setInternalBaseUrl(normalizeBaseUrl(cluster.getInternalBaseUrl()));
    registeredInstance.setStartTime(ManagementFactory.getRuntimeMXBean().getStartTime());
    registeredInstance.setLastSeenTime(now);
    try {
      Long claimed =
          redisTemplate.execute(
              CLAIM_SCRIPT,
              List.of(instanceKey(instanceId), ownerKey(instanceId)),
              registrationToken,
              jsonMapper.writeValueAsString(registeredInstance),
              Long.toString(cluster.getInstanceTtlMs()));
      if (!Long.valueOf(1).equals(claimed)) {
        throw new BizException(ResultEnum.CONFIG_ERROR, "instanceId 已被另一个活跃进程注册：" + instanceId);
      }
      redisTemplate.opsForZSet().add(INDEX_KEY, instanceId, now);
      redisTemplate.opsForZSet().removeRangeByScore(INDEX_KEY, 0, now - cluster.getInstanceTtlMs());
      // 只有已退役记录会产生实际写入；普通心跳不再 UPSERT 目录。
      catalogAccessor.reactivateIfRetired(instanceId);
      if (forceCatalogRefresh
          || now - lastCatalogRefreshTime >= cluster.getCatalogRefreshIntervalMs()) {
        catalogAccessor.touch(registeredInstance);
        lastCatalogRefreshTime = now;
      }
    } catch (BizException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new BizException(ResultEnum.CONFIG_ERROR, "在线实例注册失败", exception);
    }
  }

  public List<MonitorInstanceVO> listOnline() {
    long cutoff = System.currentTimeMillis() - properties.getCluster().getInstanceTtlMs();
    var instanceIds = redisTemplate.opsForZSet().rangeByScore(INDEX_KEY, cutoff, Double.MAX_VALUE);
    if (instanceIds == null || instanceIds.isEmpty()) {
      heartbeat();
      instanceIds = redisTemplate.opsForZSet().rangeByScore(INDEX_KEY, cutoff, Double.MAX_VALUE);
    }
    List<MonitorInstanceVO> result = new ArrayList<>();
    if (instanceIds != null) {
      for (String onlineInstanceId : instanceIds) {
        RegisteredInstance registeredInstance = read(onlineInstanceId);
        if (registeredInstance != null) {
          result.add(toVO(registeredInstance));
        }
      }
    }
    result.sort(Comparator.comparing(MonitorInstanceVO::getInstanceId));
    return result;
  }

  public RegisteredInstance require(String targetInstanceId) {
    String resolvedInstanceId =
        targetInstanceId == null || targetInstanceId.isBlank()
            ? instanceId
            : targetInstanceId.trim();
    RegisteredInstance registeredInstance = read(resolvedInstanceId);
    if (registeredInstance == null
        || System.currentTimeMillis() - registeredInstance.getLastSeenTime()
            > properties.getCluster().getInstanceTtlMs()) {
      throw new BizException(ResultEnum.PARAM_ERROR, "目标实例不在线或已过期");
    }
    return registeredInstance;
  }

  public boolean isCurrent(String targetInstanceId) {
    return Objects.equals(instanceId, targetInstanceId);
  }

  public String currentInstanceId() {
    return instanceId;
  }

  public String currentHostId() {
    return resolveHostId();
  }

  @PreDestroy
  public void unregister() {
    try {
      Long released =
          redisTemplate.execute(
              RELEASE_SCRIPT,
              List.of(instanceKey(instanceId), ownerKey(instanceId)),
              registrationToken);
      // 旧进程或重复 ID 进程不得移除当前所有者的在线索引。
      if (Long.valueOf(1).equals(released)) {
        redisTemplate.opsForZSet().remove(INDEX_KEY, instanceId);
      }
    } catch (Exception exception) {
      log.warn("服务实例注销失败，将等待注册信息按 TTL 自动过期，instanceId={}", instanceId, exception);
    }
  }

  private RegisteredInstance read(String targetInstanceId) {
    try {
      String json = redisTemplate.opsForValue().get(instanceKey(targetInstanceId));
      return json == null ? null : jsonMapper.readValue(json, RegisteredInstance.class);
    } catch (Exception exception) {
      throw new BizException(ResultEnum.CONFIG_ERROR, "读取在线实例注册信息失败", exception);
    }
  }

  private MonitorInstanceVO toVO(RegisteredInstance registeredInstance) {
    MonitorInstanceVO result = new MonitorInstanceVO();
    result.setInstanceId(registeredInstance.getInstanceId());
    result.setHostId(registeredInstance.getHostId());
    result.setApplicationVersion(registeredInstance.getApplicationVersion());
    result.setStartTime(Instant.ofEpochMilli(registeredInstance.getStartTime()).toString());
    result.setLastSeenTime(Instant.ofEpochMilli(registeredInstance.getLastSeenTime()).toString());
    result.setCurrent(isCurrent(registeredInstance.getInstanceId()));
    return result;
  }

  private String instanceKey(String targetInstanceId) {
    return INSTANCE_KEY_PREFIX + targetInstanceId;
  }

  private String ownerKey(String targetInstanceId) {
    return OWNER_KEY_PREFIX + targetInstanceId;
  }

  private String normalizeBaseUrl(String baseUrl) {
    return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  private void validateInternalBaseUrl(String baseUrl) {
    if (baseUrl == null || !(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
      throw new BizException(ResultEnum.CONFIG_ERROR, "实例内部管理地址必须使用 HTTP 或 HTTPS");
    }
    if (properties.getCluster().isRequireHttps() && !baseUrl.startsWith("https://")) {
      throw new BizException(ResultEnum.CONFIG_ERROR, "生产实例内部管理地址必须使用 HTTPS");
    }
  }

  private String resolveHostId() {
    String configuredHostId = properties.getHostId();
    String candidate =
        configuredHostId == null || configuredHostId.isBlank()
            ? systemInfo.getOperatingSystem().getNetworkParams().getHostName()
            : configuredHostId.trim();
    if (candidate == null || !candidate.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,99}")) {
      throw new BizException(ResultEnum.CONFIG_ERROR, "监控主机标识格式不合法");
    }
    return candidate.toLowerCase(Locale.ROOT);
  }

  @Data
  public static class RegisteredInstance {
    private String registrationToken;
    private String instanceId;
    private String hostId;
    private String hostName;
    private String osName;
    private String osVersion;
    private String arch;
    private String applicationName;
    private String applicationVersion;
    private String internalBaseUrl;
    private long startTime;
    private long lastSeenTime;
  }
}
