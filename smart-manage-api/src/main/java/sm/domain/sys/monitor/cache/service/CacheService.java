package sm.domain.sys.monitor.cache.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.support.CacheStat;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import sm.domain.sys.base.common.constant.BaseCacheCatalog;
import sm.domain.sys.base.common.constant.BaseCacheDefinition;
import sm.system.security.authorization.AdministratorOnly;
import sm.domain.sys.monitor.cache.model.form.CacheEntryKeyForm;
import sm.domain.sys.monitor.cache.model.form.CacheEntryListForm;
import sm.domain.sys.monitor.cache.model.vo.*;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 缓存状态和受控管理的唯一公开业务入口。 */
@Service
@AdministratorOnly
@RequiredArgsConstructor
public class CacheService {
    private static final String ALL = "ALL";
    private static final String APPLICATION = "APPLICATION";
    private static final String CACHE = "CACHE";
    private static final String INFRASTRUCTURE = "INFRASTRUCTURE";
    private static final String MONITOR_INSTANCES = "monitor-instances";
    private static final Set<String> SCOPE_TYPES = Set.of(ALL, APPLICATION, CACHE, INFRASTRUCTURE);

    private final CacheHelper cacheHelper;
    private final RedisCacheAccessor redisCacheAccessor;
    private final JsonMapper jsonMapper;

    @Value("${smart-manage.system.runtime.instance-id}")
    private String instanceId;

    public CacheOverviewVO overview() {
        return CacheOverviewVO.builder().instanceId(instanceId)
                .caches(BaseCacheCatalog.ALL.stream().map(this::assembleCache).toList())
                .collectedAt(LocalDateTime.now()).build();
    }

    public CacheRuntimeVO runtime() {
        return redisCacheAccessor.runtime();
    }

    /** 只公开可预知、受控的应用缓存和基础设施资源。 */
    public List<CacheScopeVO> scopeTree() {
        List<CacheScopeVO> applicationCaches = BaseCacheCatalog.ALL.stream()
                .map(definition -> scope(CACHE, definition.displayName(), definition.name(), List.of())).toList();
        List<CacheScopeVO> infrastructure = List.of(
                scope(INFRASTRUCTURE, "应用实例注册", MONITOR_INSTANCES, List.of()),
                scope(INFRASTRUCTURE, "登录会话（仅类别）", "login-sessions", List.of()),
                scope(INFRASTRUCTURE, "验证码（仅类别）", "captchas", List.of()),
                scope(INFRASTRUCTURE, "密码修改票据（仅类别）", "password-tickets", List.of()),
                scope(INFRASTRUCTURE, "临时代登录凭证（仅类别）", "temporary-login", List.of()));
        return List.of(scope(APPLICATION, "应用缓存", null, applicationCaches),
                scope(INFRASTRUCTURE, "基础设施缓存", null, infrastructure));
    }

    public PageData<CacheEntryVO> listPage(CacheEntryListForm form) {
        String scopeType = normalizeScopeType(form.getScopeType());
        List<CacheEntryVO> entries = new ArrayList<>();
        if (ALL.equals(scopeType) || APPLICATION.equals(scopeType)) {
            appendApplicationEntries(entries, BaseCacheCatalog.ALL);
        } else if (CACHE.equals(scopeType)) {
            appendApplicationEntries(entries, List.of(requireDefinition(form.getResourceKey())));
        }
        if ((ALL.equals(scopeType) || INFRASTRUCTURE.equals(scopeType))
                && (form.getResourceKey() == null || MONITOR_INSTANCES.equals(form.getResourceKey()))) {
            appendInfrastructureEntries(entries);
        }
        String keyword = normalize(form.getKeyword());
        List<CacheEntryVO> filtered = entries.stream()
                .filter(entry -> keyword == null || entry.getKey().toLowerCase(Locale.ROOT).contains(keyword)
                        || entry.getCacheDisplayName().toLowerCase(Locale.ROOT).contains(keyword))
                .sorted(Comparator.comparing(CacheEntryVO::getCacheDisplayName).thenComparing(CacheEntryVO::getKey))
                .toList();
        int from = Math.min((form.getPageNum() - 1) * form.getPageSize(), filtered.size());
        int to = Math.min(from + form.getPageSize(), filtered.size());
        return PageData.of(filtered.size(), form.getPageNum(), form.getPageSize(), filtered.subList(from, to));
    }

    public CacheValueVO value(CacheEntryKeyForm form) {
        // 基础设施条目没有应用缓存名；Map.ofEntries 生成的不可变 Map 不接受 null 查询键。
        BaseCacheDefinition definition = form.getCacheName() == null
                ? null
                : BaseCacheCatalog.BY_NAME.get(form.getCacheName());
        if (definition != null) return managedValue(definition, form.getKey());
        if (form.getKey() != null && form.getKey().startsWith("sm:monitor:")) {
            return redisCacheAccessor.value(form.getKey());
        }
        throw new BizException(ResultEnum.PARAM_ERROR, "不允许查看未登记或敏感的 Redis Key");
    }

    @BizLog(value = "删除应用缓存条目", recordRequest = false, recordResponse = false)
    public long delete(List<CacheEntryKeyForm> entries) {
        if (entries == null || entries.isEmpty() || entries.size() > 100) {
            throw new BizException(ResultEnum.PARAM_ERROR, "单次只能删除 1 至 100 个应用缓存条目");
        }
        List<String> redisKeys = new ArrayList<>();
        for (CacheEntryKeyForm entry : entries) {
            BaseCacheDefinition definition = requireDefinition(entry.getCacheName());
            if (!entry.getKey().startsWith(definition.name())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "缓存条目与受控缓存不匹配");
            }
            redisKeys.add(entry.getKey());
        }
        return redisCacheAccessor.delete(redisKeys);
    }

    @BizLog("清理应用缓存")
    public void clear(String cacheName) {
        redisCacheAccessor.clearByPrefix(requireDefinition(cacheName).name());
    }

    @BizLog("清理全部应用缓存")
    public void clearAll() {
        BaseCacheCatalog.ALL.forEach(definition -> redisCacheAccessor.clearByPrefix(definition.name()));
    }

    private ManagedCacheVO assembleCache(BaseCacheDefinition definition) {
        Cache<Object, Object> cache = cacheHelper.findCache(definition.name());
        if (cache == null) {
            return ManagedCacheVO.builder().name(definition.name()).displayName(definition.displayName())
                    .type(definition.cacheType().name()).description(definition.description())
                    .expireSeconds(definition.expireSeconds()).statisticsAvailable(false)
                    .currentNodeOnly(true).state("NOT_CREATED").build();
        }
        CacheStat statistics = cache.config().getMonitors().stream()
                .filter(DefaultCacheMonitor.class::isInstance).map(DefaultCacheMonitor.class::cast)
                .map(DefaultCacheMonitor::getCacheStat).findFirst().orElse(null);
        long count = statistics == null ? 0 : statistics.getGetCount();
        return ManagedCacheVO.builder().name(definition.name()).displayName(definition.displayName())
                .type(definition.cacheType().name()).description(definition.description())
                .expireSeconds(cache.config().getExpireAfterWriteInMillis() / 1000)
                .statisticsAvailable(statistics != null).currentNodeOnly(true)
                .state(statistics == null ? "UNAVAILABLE" : count == 0 ? "IDLE" : "ACTIVE")
                .statStartedAt(statistics == null ? null : localTime(statistics.getStatStartTime()))
                .statEndedAt(statistics == null ? null : localTime(statistics.getStatEndTime()))
                .getCount(count).hitCount(statistics == null ? 0 : statistics.getGetHitCount())
                .missCount(statistics == null ? 0 : statistics.getGetMissCount())
                .failCount(statistics == null ? 0 : statistics.getGetFailCount())
                .hitRate(statistics == null ? 0 : statistics.hitRate())
                .qps(statistics == null ? 0 : statistics.qps())
                .averageGetTime(statistics == null ? 0 : statistics.avgGetTime()).build();
    }

    private void appendApplicationEntries(List<CacheEntryVO> entries, List<BaseCacheDefinition> definitions) {
        List<String> prefixes = definitions.stream().map(BaseCacheDefinition::name).toList();
        for (RedisCacheAccessor.RedisEntry entry : redisCacheAccessor.scanEntries(prefixes)) {
            BaseCacheDefinition definition = BaseCacheCatalog.ALL.stream()
                    .filter(item -> entry.key().startsWith(item.name())).findFirst().orElse(null);
            if (definition != null) entries.add(toEntry(entry, definition.name(), definition.displayName(),
                    !definition.sensitiveValue()));
        }
    }

    private void appendInfrastructureEntries(List<CacheEntryVO> entries) {
        redisCacheAccessor.monitorInstanceEntries().forEach(entry ->
                entries.add(toEntry(entry, null, "应用实例注册", true)));
    }

    private CacheEntryVO toEntry(RedisCacheAccessor.RedisEntry entry, String cacheName,
                                 String displayName, boolean readable) {
        return CacheEntryVO.builder().identity("REDIS|" + (cacheName == null ? "" : cacheName) + "|" + entry.key())
                .storage("REDIS").cacheName(cacheName).cacheDisplayName(displayName).key(entry.key())
                .type(entry.type()).ttl(entry.ttl()).memoryBytes(entry.memoryBytes())
                .valueReadable(readable).currentNodeOnly(false).build();
    }

    private CacheValueVO managedValue(BaseCacheDefinition definition, String physicalKey) {
        if (definition.sensitiveValue()) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "安全敏感缓存不允许查看 Value");
        }
        if (physicalKey == null || !physicalKey.startsWith(definition.name())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "应用缓存 Key 格式不正确");
        }
        Cache<Object, Object> cache = cacheHelper.findCache(definition.name());
        if (cache == null) throw new BizException(ResultEnum.NOT_FOUND, "缓存实例尚未创建");
        Object value = cache.get(physicalKey.substring(definition.name().length()));
        if (value == null) throw new BizException(ResultEnum.NOT_FOUND, "缓存条目不存在");
        try {
            String json = jsonMapper.writeValueAsString(value);
            boolean truncated = json.length() > 64 * 1024;
            return CacheValueVO.builder().key(physicalKey).type("object").truncated(truncated)
                    .items(List.of(CacheValueItemVO.builder()
                            .value(truncated ? json.substring(0, 64 * 1024) : json).base64(false).build())).build();
        } catch (JacksonException exception) {
            throw new BizException(ResultEnum.SERVER_ERROR, "缓存值序列化失败");
        }
    }

    private CacheScopeVO scope(String type, String name, String resourceKey, List<CacheScopeVO> children) {
        return CacheScopeVO.builder().type(type).name(name).resourceKey(resourceKey).children(children).build();
    }

    private BaseCacheDefinition requireDefinition(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "不允许管理基础设施或未登记缓存");
        }
        BaseCacheDefinition definition = BaseCacheCatalog.BY_NAME.get(name);
        if (definition == null) throw new BizException(ResultEnum.PARAM_ERROR, "不允许管理该缓存");
        return definition;
    }

    private String normalizeScopeType(String value) {
        String normalized = value == null || value.isBlank() ? ALL : value.trim().toUpperCase(Locale.ROOT);
        if (!SCOPE_TYPES.contains(normalized)) throw new BizException(ResultEnum.PARAM_ERROR, "缓存范围不合法");
        return normalized;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDateTime localTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}
