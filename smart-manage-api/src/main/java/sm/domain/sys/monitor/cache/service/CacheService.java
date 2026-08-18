package sm.domain.sys.monitor.cache.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.support.CacheStat;
import com.alicp.jetcache.support.DefaultCacheMonitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.app.model.vo.AppVO;
import sm.domain.sys.base.app.model.vo.CloudAppsVO;
import sm.domain.sys.base.app.service.AppService;
import sm.domain.sys.monitor.cache.model.vo.CacheOverviewVO;
import sm.domain.sys.monitor.cache.model.vo.ManagedCacheVO;
import sm.domain.sys.monitor.cache.model.form.CacheEntryKeyForm;
import sm.domain.sys.monitor.cache.model.form.CacheEntryListForm;
import sm.domain.sys.monitor.cache.model.vo.CacheEntryVO;
import sm.domain.sys.monitor.cache.model.vo.CacheRuntimeVO;
import sm.domain.sys.monitor.cache.model.vo.CacheScopeVO;
import sm.domain.sys.monitor.cache.model.vo.CacheValueItemVO;
import sm.domain.sys.monitor.cache.model.vo.CacheValueVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import sm.system.response.ResultEnum;
import sm.system.response.PageData;
import sm.system.query.ListSqlQuery;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.math.BigDecimal;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** 缓存监控与受控管理的唯一公开业务入口。 */
@Service
@RequiredArgsConstructor
public class CacheService {
    private static final Map<String, CacheDefinition> MANAGED_CACHES = managedCaches();
    private static final Set<String> SCOPE_TYPES = Set.of("ALL", "CLOUD", "APP", "OTHER");
    private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
            "key", ListSqlQuery.string("key", true),
            "cacheDisplayName", ListSqlQuery.string("cacheDisplayName", false),
            "storage", ListSqlQuery.enumeration("storage", false),
            "type", ListSqlQuery.string("type", false),
            "ttl", ListSqlQuery.number("ttl", true),
            "memoryBytes", ListSqlQuery.number("memoryBytes", true));

    private final CacheHelper cacheHelper;
    private final CurrentUserContext currentUserContext;
    private final RedisCacheAccessor redisCacheAccessor;
    private final JsonMapper jsonMapper;
    private final AppService appService;

    public CacheOverviewVO overview() {
        currentUserContext.checkAdministrator();
        List<ManagedCacheVO> caches = MANAGED_CACHES.values().stream().map(this::assembleCache).toList();
        return CacheOverviewVO.builder().caches(caches).collectedAt(LocalDateTime.now()).build();
    }

    public CacheRuntimeVO runtime() {
        currentUserContext.checkAdministrator();
        return redisCacheAccessor.runtime();
    }

    public List<CacheScopeVO> scopeTree() {
        currentUserContext.checkAdministrator();
        List<CacheScopeVO> clouds = new ArrayList<>();
        for (CloudAppsVO cloud : appService.getAllCloudApps()) {
            List<CacheScopeVO> applications = cloud.getAppList().stream()
                    .map(application -> CacheScopeVO.builder().type("APP").name(application.getName())
                            .cloudNumber(cloud.getNumber()).appNumber(application.getNumber()).children(List.of())
                            .build())
                    .toList();
            clouds.add(CacheScopeVO.builder().type("CLOUD").name(cloud.getName())
                    .cloudNumber(cloud.getNumber()).children(applications).build());
        }
        clouds.add(CacheScopeVO.builder().type("OTHER").name("其他缓存").children(List.of()).build());
        return clouds;
    }

    public PageData<CacheEntryVO> listPage(CacheEntryListForm form) {
        currentUserContext.checkAdministrator();
        String scopeType = normalizeScopeType(form.getScopeType());
        List<CloudAppsVO> cloudApps = appService.getAllCloudApps();
        List<AppScope> applicationScopes = applicationScopes(cloudApps);
        validateScope(form, scopeType, cloudApps, applicationScopes);
        List<CacheEntryVO> entries = new ArrayList<>();
        appendLocalEntries(entries);
        appendRedisEntries(entries);
        String keyword = normalize(form.getKeyword());
        ListSqlQuery listQuery = ListSqlQuery.of(form, LIST_FIELDS);
        List<CacheEntryVO> filtered = entries.stream()
                .filter(entry -> matchesScope(entry, form, scopeType, applicationScopes))
                .filter(entry -> keyword == null
                        || entry.getKey().toLowerCase(Locale.ROOT).contains(keyword)
                        || normalize(entry.getCacheDisplayName()) != null
                        && normalize(entry.getCacheDisplayName()).contains(keyword))
                .filter(entry -> listQuery.conditions().stream().allMatch(condition -> matches(entry, condition)))
                .sorted(cacheComparator(listQuery))
                .toList();
        int fromIndex = Math.min((form.getPageNum() - 1) * form.getPageSize(), filtered.size());
        int toIndex = Math.min(fromIndex + form.getPageSize(), filtered.size());
        return PageData.of(filtered.size(), form.getPageNum(), form.getPageSize(), filtered.subList(fromIndex, toIndex));
    }

    private boolean matches(CacheEntryVO entry, ListSqlQuery.Condition condition) {
        Object rawValue = switch (condition.column()) {
            case "key" -> entry.getKey();
            case "cacheDisplayName" -> entry.getCacheDisplayName();
            case "storage" -> entry.getStorage();
            case "type" -> entry.getType();
            case "ttl" -> entry.getTtl();
            case "memoryBytes" -> entry.getMemoryBytes();
            default -> null;
        };
        return switch (condition.operator()) {
            case EMPTY -> rawValue == null || String.valueOf(rawValue).isEmpty();
            case NOT_EMPTY -> rawValue != null && !String.valueOf(rawValue).isEmpty();
            case IN -> rawValue != null && condition.values().stream()
                    .anyMatch(value -> String.valueOf(value).equals(String.valueOf(rawValue)));
            case CONTAINS -> text(rawValue).contains(text(condition.value()));
            case NOT_CONTAINS -> !text(rawValue).contains(text(condition.value()));
            case STARTS_WITH -> text(rawValue).startsWith(text(condition.value()));
            case ENDS_WITH -> text(rawValue).endsWith(text(condition.value()));
            case EQ -> compare(rawValue, condition.value()) == 0;
            case NE -> compare(rawValue, condition.value()) != 0;
            case GT -> compare(rawValue, condition.value()) > 0;
            case GE -> compare(rawValue, condition.value()) >= 0;
            case LT -> compare(rawValue, condition.value()) < 0;
            case LE -> compare(rawValue, condition.value()) <= 0;
            default -> false;
        };
    }

    private Comparator<CacheEntryVO> cacheComparator(ListSqlQuery query) {
        Comparator<CacheEntryVO> fallback = Comparator.comparing(CacheEntryVO::getStorage)
                .thenComparing(CacheEntryVO::getKey);
        if (query.sortColumn() == null) return fallback;
        Comparator<CacheEntryVO> comparator = switch (query.sortColumn()) {
            case "key" -> Comparator.comparing(CacheEntryVO::getKey, Comparator.nullsLast(String::compareTo));
            case "ttl" -> Comparator.comparing(CacheEntryVO::getTtl, Comparator.nullsLast(Long::compareTo));
            case "memoryBytes" -> Comparator.comparing(CacheEntryVO::getMemoryBytes, Comparator.nullsLast(Long::compareTo));
            default -> fallback;
        };
        return "DESC".equals(query.sortOrder()) ? comparator.reversed().thenComparing(fallback)
                : comparator.thenComparing(fallback);
    }

    private int compare(Object left, Object right) {
        if (left == null) return -1;
        if (left instanceof Number) {
            return new BigDecimal(String.valueOf(left)).compareTo(new BigDecimal(String.valueOf(right)));
        }
        return String.valueOf(left).compareTo(String.valueOf(right));
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private List<AppScope> applicationScopes(List<CloudAppsVO> cloudApps) {
        List<AppScope> scopes = new ArrayList<>();
        for (CloudAppsVO cloud : cloudApps) {
            for (AppVO application : cloud.getAppList()) {
                scopes.add(new AppScope(cloud.getNumber(), application.getNumber()));
            }
        }
        return scopes;
    }

    private String normalizeScopeType(String scopeType) {
        String normalized = scopeType == null || scopeType.isBlank()
                ? "ALL" : scopeType.trim().toUpperCase(Locale.ROOT);
        if (!SCOPE_TYPES.contains(normalized)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "缓存归属范围不合法");
        }
        return normalized;
    }

    private void validateScope(CacheEntryListForm form, String scopeType, List<CloudAppsVO> cloudApps,
                               List<AppScope> applicationScopes) {
        if ("CLOUD".equals(scopeType)) {
            boolean exists = cloudApps.stream().anyMatch(cloud -> cloud.getNumber().equals(form.getCloudNumber()));
            if (!exists) {
                throw new BizException(ResultEnum.PARAM_ERROR, "缓存所属云不存在");
            }
        }
        if ("APP".equals(scopeType)) {
            boolean exists = applicationScopes.stream().anyMatch(scope ->
                    scope.cloudNumber().equals(form.getCloudNumber())
                            && scope.appNumber().equals(form.getAppNumber()));
            if (!exists) {
                throw new BizException(ResultEnum.PARAM_ERROR, "缓存所属应用不存在");
            }
        }
    }

    private boolean matchesScope(CacheEntryVO entry, CacheEntryListForm form, String scopeType,
                                 List<AppScope> applicationScopes) {
        if ("ALL".equals(scopeType)) {
            return true;
        }
        if ("OTHER".equals(scopeType)) {
            return applicationScopes.stream().noneMatch(scope -> belongsTo(entry, scope));
        }
        if ("CLOUD".equals(scopeType)) {
            return applicationScopes.stream()
                    .filter(scope -> scope.cloudNumber().equals(form.getCloudNumber()))
                    .anyMatch(scope -> belongsTo(entry, scope));
        }
        AppScope requested = new AppScope(form.getCloudNumber(), form.getAppNumber());
        return belongsTo(entry, requested);
    }

    private boolean belongsTo(CacheEntryVO entry, AppScope scope) {
        String classifiedKey = "LOCAL".equals(entry.getStorage()) ? entry.getCacheName() : entry.getKey();
        return classifiedKey != null && classifiedKey.startsWith(scope.prefix());
    }

    public CacheValueVO value(CacheEntryKeyForm form) {
        currentUserContext.checkAdministrator();
        if ("REDIS".equalsIgnoreCase(form.getStorage())) {
            CacheDefinition definition = remoteDefinition(form.getKey());
            if (definition != null) {
                return managedRemoteValue(definition, form.getKey());
            }
            return redisCacheAccessor.value(form.getKey());
        }
        CacheDefinition definition = requireLocalDefinition(form.getCacheName());
        if (definition.sensitiveValue()) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "安全敏感缓存不允许查看 Value");
        }
        Object value = findLocalEntry(definition, form.getKey()).getValue();
        String json;
        try {
            json = jsonMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new BizException(ResultEnum.SERVER_ERROR, "缓存值序列化失败");
        }
        boolean truncated = json.length() > 64 * 1024;
        String preview = truncated ? json.substring(0, 64 * 1024) : json;
        return CacheValueVO.builder().key(form.getKey()).type("object").truncated(truncated)
                .items(List.of(CacheValueItemVO.builder().value(preview).base64(false).build())).build();
    }

    private CacheValueVO managedRemoteValue(CacheDefinition definition, String physicalKey) {
        if (definition.sensitiveValue()) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "安全敏感缓存不允许查看 Value");
        }
        String logicalKey = physicalKey.substring(definition.name().length());
        if (logicalKey.isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "应用缓存 Key 格式不正确");
        }
        Cache<Object, Object> cache = cacheHelper.getCache(definition.name(), CacheType.REMOTE);
        Object value = cache.get(logicalKey);
        if (value == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "缓存条目不存在");
        }
        String json;
        try {
            json = jsonMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new BizException(ResultEnum.SERVER_ERROR, "缓存值序列化失败");
        }
        boolean truncated = json.length() > 64 * 1024;
        String preview = truncated ? json.substring(0, 64 * 1024) : json;
        return CacheValueVO.builder().key(physicalKey).type("object").truncated(truncated)
                .items(List.of(CacheValueItemVO.builder().value(preview).base64(false).build())).build();
    }

    @BizLog(value = "删除缓存条目", recordRequest = false, recordResponse = false)
    public long delete(List<CacheEntryKeyForm> entries) {
        currentUserContext.checkAdministrator();
        if (entries == null || entries.isEmpty() || entries.size() > 100) {
            throw new BizException(ResultEnum.PARAM_ERROR, "单次只能删除 1 至 100 个缓存条目");
        }
        List<String> redisKeys = new ArrayList<>();
        long deleted = 0;
        for (CacheEntryKeyForm entry : entries) {
            if ("REDIS".equalsIgnoreCase(entry.getStorage())) {
                redisKeys.add(entry.getKey());
                continue;
            }
            CacheDefinition definition = requireLocalDefinition(entry.getCacheName());
            Map.Entry<?, ?> localEntry = findLocalEntry(definition, entry.getKey());
            Cache<Object, Object> cache = cacheHelper.getCache(definition.name(), definition.cacheType());
            if (cache.remove(localEntry.getKey())) {
                deleted++;
            }
        }
        if (!redisKeys.isEmpty()) {
            deleted += redisCacheAccessor.delete(redisKeys);
        }
        return deleted;
    }

    @BizLog("清理应用缓存")
    public void clear(String cacheName) {
        currentUserContext.checkAdministrator();
        clear(requireDefinition(cacheName));
    }

    @BizLog("清理全部应用缓存")
    public void clearAll() {
        currentUserContext.checkAdministrator();
        MANAGED_CACHES.values().forEach(this::clear);
    }

    private ManagedCacheVO assembleCache(CacheDefinition definition) {
        Long estimatedSize = null;
        Cache<Object, Object> cache = cacheHelper.getCache(definition.name(), definition.cacheType());
        CacheStat statistics = cache.config().getMonitors().stream()
                .filter(DefaultCacheMonitor.class::isInstance)
                .map(DefaultCacheMonitor.class::cast)
                .map(DefaultCacheMonitor::getCacheStat)
                .findFirst()
                .orElse(null);
        if (definition.cacheType() == CacheType.LOCAL) {
            com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine =
                    cache.unwrap(com.github.benmanes.caffeine.cache.Cache.class);
            if (caffeine != null) {
                estimatedSize = caffeine.estimatedSize();
            }
        }
        return ManagedCacheVO.builder()
                .name(definition.name()).displayName(definition.displayName())
                .type(definition.cacheType().name()).description(definition.description())
                .expireSeconds(definition.expireSeconds()).estimatedSize(estimatedSize)
                .statisticsAvailable(statistics != null)
                .getCount(statistics == null ? 0 : statistics.getGetCount())
                .hitCount(statistics == null ? 0 : statistics.getGetHitCount())
                .missCount(statistics == null ? 0 : statistics.getGetMissCount())
                .failCount(statistics == null ? 0 : statistics.getGetFailCount())
                .hitRate(statistics == null ? 0 : statistics.hitRate())
                .qps(statistics == null ? 0 : statistics.qps())
                .averageGetTime(statistics == null ? 0 : statistics.avgGetTime())
                .currentNodeOnly(definition.cacheType() == CacheType.LOCAL).build();
    }

    private void appendLocalEntries(List<CacheEntryVO> entries) {
        MANAGED_CACHES.values().stream().filter(definition -> definition.cacheType() == CacheType.LOCAL)
                .forEach(definition -> {
                    Cache<Object, Object> cache = cacheHelper.getCache(definition.name(), definition.cacheType());
                    com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine =
                            cache.unwrap(com.github.benmanes.caffeine.cache.Cache.class);
                    if (caffeine == null) {
                        return;
                    }
                    caffeine.asMap().keySet().forEach(key -> entries.add(CacheEntryVO.builder()
                            .identity("LOCAL|" + definition.name() + "|" + key)
                            .storage("LOCAL").cacheName(definition.name()).cacheDisplayName(definition.displayName())
                            .key(String.valueOf(key)).type("object").ttl(null).memoryBytes(null)
                            .valueReadable(!definition.sensitiveValue()).currentNodeOnly(true).build()));
                });
    }

    private void appendRedisEntries(List<CacheEntryVO> entries) {
        redisCacheAccessor.scanEntries().forEach(redisEntry -> {
                    CacheDefinition definition = remoteDefinition(redisEntry.key());
                    entries.add(CacheEntryVO.builder().identity("REDIS||" + redisEntry.key()).storage("REDIS")
                            .cacheName(definition == null ? null : definition.name())
                            .cacheDisplayName(definition == null ? "Redis Key" : definition.displayName())
                            .key(redisEntry.key()).type(redisEntry.type()).ttl(redisEntry.ttl())
                            .memoryBytes(redisEntry.memoryBytes()).valueReadable(redisEntry.valueReadable())
                            .currentNodeOnly(false).build());
        });
    }

    private CacheDefinition remoteDefinition(String key) {
        return MANAGED_CACHES.values().stream()
                .filter(definition -> definition.cacheType() == CacheType.REMOTE && key.startsWith(definition.name()))
                .findFirst().orElse(null);
    }

    private CacheDefinition requireLocalDefinition(String cacheName) {
        CacheDefinition definition = requireDefinition(cacheName);
        if (definition.cacheType() != CacheType.LOCAL) {
            throw new BizException(ResultEnum.PARAM_ERROR, "缓存存储位置不匹配");
        }
        return definition;
    }

    private Map.Entry<?, ?> findLocalEntry(CacheDefinition definition, String key) {
        Cache<Object, Object> cache = cacheHelper.getCache(definition.name(), definition.cacheType());
        com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine = cache.unwrap(com.github.benmanes.caffeine.cache.Cache.class);
        if (caffeine == null) {
            throw new BizException(ResultEnum.SERVER_ERROR, "本地缓存实现不支持条目管理");
        }
        return caffeine.asMap().entrySet().stream().filter(entry -> String.valueOf(entry.getKey()).equals(key))
                .findFirst().orElseThrow(() -> new BizException(ResultEnum.NOT_FOUND, "缓存条目不存在"));
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private CacheDefinition requireDefinition(String cacheName) {
        CacheDefinition definition = MANAGED_CACHES.get(cacheName);
        if (definition == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "不允许管理该缓存");
        }
        return definition;
    }

    private void clear(CacheDefinition definition) {
        if (definition.cacheType() == CacheType.LOCAL) {
            Cache<Object, Object> cache = cacheHelper.getCache(definition.name(), definition.cacheType());
            com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine =
                    cache.unwrap(com.github.benmanes.caffeine.cache.Cache.class);
            if (caffeine == null) {
                throw new BizException(ResultEnum.SERVER_ERROR, "本地缓存实现不支持整体清理");
            }
            caffeine.invalidateAll();
            return;
        }
        // JetCache Redis Key 使用缓存名作为前缀；访问器只接收受控目录校验后的前缀。
        redisCacheAccessor.clearByPrefix(definition.name());
    }

    private static Map<String, CacheDefinition> managedCaches() {
        Map<String, CacheDefinition> caches = new LinkedHashMap<>();
        caches.put(BaseCacheName.USER_INFO, new CacheDefinition(BaseCacheName.USER_INFO, "用户信息", CacheType.REMOTE, "用户基础信息", 3600, true));
        caches.put(BaseCacheName.SYS_PARAM, new CacheDefinition(BaseCacheName.SYS_PARAM, "系统参数", CacheType.REMOTE, "系统参数快照", 1800, false));
        caches.put(BaseCacheName.BASIC_DATA_OPTIONS, new CacheDefinition(BaseCacheName.BASIC_DATA_OPTIONS, "基础数据选项", CacheType.REMOTE, "基础数据下拉选项", 1800, false));
        return Map.copyOf(caches);
    }

    private record CacheDefinition(String name, String displayName, CacheType cacheType, String description,
                                   long expireSeconds, boolean sensitiveValue) {
    }

    private record AppScope(String cloudNumber, String appNumber) {
        String prefix() {
            return cloudNumber + ":" + appNumber + ":";
        }
    }
}
