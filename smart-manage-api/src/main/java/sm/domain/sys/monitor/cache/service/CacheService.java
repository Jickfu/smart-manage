package sm.domain.sys.monitor.cache.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.monitor.cache.model.vo.CacheOverviewVO;
import sm.domain.sys.monitor.cache.model.vo.ManagedCacheVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import sm.system.response.ResultEnum;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/** 受控应用缓存查询与清理服务。 */
@Service
@RequiredArgsConstructor
public class CacheService {
    private static final Map<String, CacheDefinition> MANAGED_CACHES = managedCaches();

    private final CacheHelper cacheHelper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CurrentUserContext currentUserContext;

    public CacheOverviewVO overview() {
        List<ManagedCacheVO> caches = MANAGED_CACHES.values().stream().map(this::assembleCache).toList();
        return CacheOverviewVO.builder().caches(caches).collectedAt(LocalDateTime.now()).build();
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
        boolean statisticsAvailable = false;
        if (definition.cacheType() == CacheType.LOCAL) {
            Cache<Object, Object> cache = cacheHelper.getCache(definition.name(), definition.cacheType());
            com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine =
                    cache.unwrap(com.github.benmanes.caffeine.cache.Cache.class);
            if (caffeine != null) {
                estimatedSize = caffeine.estimatedSize();
                statisticsAvailable = caffeine.stats().requestCount() > 0;
            }
        }
        return ManagedCacheVO.builder()
                .name(definition.name()).displayName(definition.displayName())
                .type(definition.cacheType().name()).description(definition.description())
                .expireSeconds(definition.expireSeconds()).estimatedSize(estimatedSize)
                .statisticsAvailable(statisticsAvailable)
                .currentNodeOnly(definition.cacheType() == CacheType.LOCAL).build();
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
        clearRemoteCache(definition.name());
    }

    /** JetCache Redis Key 使用缓存名作为前缀；这里只允许清理受控目录中的前缀。 */
    private void clearRemoteCache(String cacheName) {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().match(cacheName + "*").count(500).build())) {
                List<byte[]> batch = new ArrayList<>(500);
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() == 500) {
                        connection.del(batch.toArray(byte[][]::new));
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    connection.del(batch.toArray(byte[][]::new));
                }
            }
            return null;
        });
    }

    private static Map<String, CacheDefinition> managedCaches() {
        Map<String, CacheDefinition> caches = new LinkedHashMap<>();
        caches.put(CacheConstant.USER_INFO, new CacheDefinition(CacheConstant.USER_INFO, "用户信息", CacheType.REMOTE, "用户基础信息", 3600));
        caches.put(CacheConstant.SYS_PARAM, new CacheDefinition(CacheConstant.SYS_PARAM, "系统参数", CacheType.LOCAL, "系统参数快照", 1800));
        caches.put(CacheConstant.UI_CONFIG, new CacheDefinition(CacheConstant.UI_CONFIG, "界面配置", CacheType.LOCAL, "系统界面配置", 1800));
        caches.put(CacheConstant.FILE_CONFIG, new CacheDefinition(CacheConstant.FILE_CONFIG, "文件配置", CacheType.LOCAL, "文件存储配置", 1800));
        caches.put(CacheConstant.BASIC_DATA_OPTIONS, new CacheDefinition(CacheConstant.BASIC_DATA_OPTIONS, "基础数据选项", CacheType.LOCAL, "基础数据下拉选项", 1800));
        return Map.copyOf(caches);
    }

    private record CacheDefinition(String name, String displayName, CacheType cacheType, String description, long expireSeconds) {
    }
}
