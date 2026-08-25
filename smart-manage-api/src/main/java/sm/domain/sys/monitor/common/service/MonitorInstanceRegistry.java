package sm.domain.sys.monitor.common.service;

import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.common.config.MonitorClusterProperties;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.json.JsonMapper;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Locale;
import oshi.SystemInfo;

/** Redis 在线实例注册表；实例详情独立 TTL，索引只用于发现候选实例。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonitorInstanceRegistry {
    private static final String INDEX_KEY = "sm:monitor:instances";
    private static final String INSTANCE_KEY_PREFIX = "sm:monitor:instance:";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final MonitorClusterProperties properties;
    private final MonitorCatalogAccessor catalogAccessor;
    private final SystemInfo systemInfo = new SystemInfo();

    @Value("${smart-manage.instance-id}")
    private String instanceId;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:unknown}")
    private String applicationVersion;

    @Value("${smart-manage.monitor.host-id:}")
    private String configuredHostId;

    @EventListener(ApplicationReadyEvent.class)
    public void registerWhenReady() {
        // Redis 是系统基础设施，启动阶段注册失败必须中止启动，禁止伪装成可用实例。
        heartbeat();
    }

    @Scheduled(fixedDelayString = "${smart-manage.monitor.cluster.heartbeat-interval-ms:10000}")
    public void scheduledHeartbeat() {
        // 运行中断连时保留进程以便健康检查、告警和连接池自动恢复，但心跳失败不得吞掉。
        heartbeat();
    }

    public void heartbeat() {
        validateInternalBaseUrl(properties.getInternalBaseUrl());
        long now = System.currentTimeMillis();
        RegisteredInstance registeredInstance = new RegisteredInstance();
        registeredInstance.setInstanceId(instanceId);
        registeredInstance.setHostId(resolveHostId());
        registeredInstance.setHostName(systemInfo.getOperatingSystem().getNetworkParams().getHostName());
        registeredInstance.setOsName(systemInfo.getOperatingSystem().getFamily());
        registeredInstance.setOsVersion(systemInfo.getOperatingSystem().getVersionInfo().getVersion());
        registeredInstance.setArch(System.getProperty("os.arch"));
        registeredInstance.setApplicationName(applicationName);
        registeredInstance.setApplicationVersion(applicationVersion);
        registeredInstance.setInternalBaseUrl(normalizeBaseUrl(properties.getInternalBaseUrl()));
        registeredInstance.setStartTime(ManagementFactory.getRuntimeMXBean().getStartTime());
        registeredInstance.setLastSeenTime(now);
        try {
            redisTemplate.opsForValue().set(instanceKey(instanceId),
                    jsonMapper.writeValueAsString(registeredInstance),
                    Duration.ofMillis(properties.getInstanceTtlMs()));
            redisTemplate.opsForZSet().add(INDEX_KEY, instanceId, now);
            redisTemplate.opsForZSet().removeRangeByScore(INDEX_KEY, 0, now - properties.getInstanceTtlMs());
            catalogAccessor.touch(registeredInstance);
        } catch (Exception exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "在线实例注册失败", exception);
        }
    }

    public List<MonitorInstanceVO> listOnline() {
        long cutoff = System.currentTimeMillis() - properties.getInstanceTtlMs();
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
        String resolvedInstanceId = targetInstanceId == null || targetInstanceId.isBlank()
                ? instanceId
                : targetInstanceId.trim();
        RegisteredInstance registeredInstance = read(resolvedInstanceId);
        if (registeredInstance == null
                || System.currentTimeMillis() - registeredInstance.getLastSeenTime() > properties.getInstanceTtlMs()) {
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
            redisTemplate.delete(instanceKey(instanceId));
            redisTemplate.opsForZSet().remove(INDEX_KEY, instanceId);
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
        result.setStartTime(TIME_FORMATTER.format(Instant.ofEpochMilli(registeredInstance.getStartTime())));
        result.setLastSeenTime(TIME_FORMATTER.format(Instant.ofEpochMilli(registeredInstance.getLastSeenTime())));
        result.setCurrent(isCurrent(registeredInstance.getInstanceId()));
        return result;
    }

    private String instanceKey(String targetInstanceId) {
        return INSTANCE_KEY_PREFIX + targetInstanceId;
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private void validateInternalBaseUrl(String baseUrl) {
        if (baseUrl == null || !(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "实例内部管理地址必须使用 HTTP 或 HTTPS");
        }
        if (properties.isRequireHttps() && !baseUrl.startsWith("https://")) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "生产实例内部管理地址必须使用 HTTPS");
        }
    }

    private String resolveHostId() {
        String candidate = configuredHostId == null || configuredHostId.isBlank()
                ? systemInfo.getOperatingSystem().getNetworkParams().getHostName()
                : configuredHostId.trim();
        if (candidate == null || !candidate.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,99}")) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "监控主机标识格式不合法");
        }
        return candidate.toLowerCase(Locale.ROOT);
    }

    @Data
    public static class RegisteredInstance {
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
