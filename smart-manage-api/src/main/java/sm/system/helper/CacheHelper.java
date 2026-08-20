package sm.system.helper;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;
import com.alicp.jetcache.anno.CacheConsts;

/**
 * 统一缓存助手 —— 封装 JetCache CacheManager，提供懒加载的缓存获取。
 * <p>
 * 懒创建 + 线程安全（ConcurrentHashMap），Service 中无需 {@code @PostConstruct} 初始化。
 *
 * @author Chekfu
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheHelper {
    private final CacheManager cacheManager;
    private final ConcurrentHashMap<CacheIdentity, Cache<?, ?>> map = new ConcurrentHashMap<>();

    /**
     * 按名称和类型获取缓存实例（懒创建，首次调用时才创建）。
     *
     * @param name      缓存名称，需与 {@code @Cached(name = "...")} 一致
     * @param cacheType 缓存类型，需与 {@code @Cached(cacheType = ...)} 一致
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name, CacheType cacheType, long expireSeconds) {
        CacheIdentity identity = new CacheIdentity(name, cacheType, expireSeconds);
        return (Cache<K, V>) map.computeIfAbsent(identity,
                ignored -> cacheManager.getOrCreateCache(
                        QuickConfig.newBuilder(name).cacheType(cacheType)
                                .expire(Duration.ofSeconds(expireSeconds)).build()));
    }

    /** 只读取已经由业务创建的缓存，监控调用不得改变缓存实例及其 TTL。 */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> findCache(String name) {
        return (Cache<K, V>) cacheManager.getCache(CacheConsts.DEFAULT_AREA, name);
    }

    private record CacheIdentity(String name, CacheType cacheType, long expireSeconds) {
    }
}
