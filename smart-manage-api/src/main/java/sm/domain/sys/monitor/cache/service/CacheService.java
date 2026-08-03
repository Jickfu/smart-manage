package sm.domain.sys.monitor.cache.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.monitor.cache.model.vo.CacheStatsVO;
import sm.domain.sys.monitor.cache.model.vo.CaffeineCacheVO;
import sm.domain.sys.monitor.cache.model.vo.RedisInfoVO;
import sm.domain.sys.monitor.cache.model.vo.RedisKeyVO;
import sm.system.aop.log.BizLog;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 缓存管理服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;

    /** 单次扫描最大 key 数 */
    private static final int MAX_SCAN_KEYS = 500;

    /** 已知的 JetCache LOCAL 缓存名列表 */
    private static final List<String> LOCAL_CACHE_NAMES = List.of(
            CacheConstant.SYS_PARAM,
            CacheConstant.UI_CONFIG,
            CacheConstant.FILE_CONFIG,
            CacheConstant.BASIC_DATA_OPTIONS);

    // ==================== 统计 ====================

    /** 获取缓存统计（Caffeine + Redis） */
    public CacheStatsVO getStats() {
        CacheStatsVO vo = new CacheStatsVO();

        // Caffeine 本地缓存统计（从 JetCache 获取）
        vo.setCaffeineCaches(buildCaffeineStats());

        // Redis 信息
        try {
            vo.setRedisInfo(buildRedisInfo());
        } catch (Exception e) {
            log.warn("获取 Redis 信息失败: {}", e.getMessage());
            vo.setRedisInfo(new RedisInfoVO());
        }

        return vo;
    }

    private List<CaffeineCacheVO> buildCaffeineStats() {
        List<CaffeineCacheVO> list = new ArrayList<>();
        for (String cacheName : LOCAL_CACHE_NAMES) {
            try {
                Cache<?, ?> cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    // 通过 unwrap 获取底层 Caffeine Cache 实例
                    com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine = cache.unwrap(com.github.benmanes.caffeine.cache.Cache.class);
                    if (caffeine != null) {
                        CaffeineCacheVO vo = new CaffeineCacheVO();
                        vo.setName(cacheName);
                        vo.setEstimatedSize(caffeine.estimatedSize());
                        vo.setHitCount(caffeine.stats().hitCount());
                        vo.setMissCount(caffeine.stats().missCount());
                        vo.setHitRate(caffeine.stats().hitRate());
                        vo.setEvictionCount(caffeine.stats().evictionCount());
                        vo.setRequestCount(caffeine.stats().requestCount());
                        list.add(vo);
                    }
                }
            } catch (Exception e) {
                log.warn("获取 JetCache 本地缓存 [{}] 统计失败: {}", cacheName, e.getMessage());
            }
        }
        return list;
    }

    private RedisInfoVO buildRedisInfo() {
        RedisInfoVO vo = new RedisInfoVO();

        Properties info = redisTemplate.execute((RedisCallback<Properties>) RedisConnection::info);
        if (info != null) {
            vo.setVersion(info.getProperty("redis_version", "-"));
            String uptime = info.getProperty("uptime_in_days", "0");
            vo.setUptimeDays(Long.parseLong(uptime));
            vo.setUsedMemoryHuman(info.getProperty("used_memory_human", "-"));
            vo.setConnectedClients(Integer.parseInt(info.getProperty("connected_clients", "0")));
        }

        Long dbSize = redisTemplate.execute(RedisConnection::dbSize);
        vo.setDbSize(dbSize != null ? dbSize : 0);

        return vo;
    }

    // ==================== Caffeine 操作 ====================

    /** 清除 JetCache Caffeine 本地缓存 */
    @BizLog("清理本地缓存")
    public void clearCaffeine(String cacheName) {
        if (cacheName == null || cacheName.isBlank()) {
            // 清空所有已知本地缓存
            for (String name : LOCAL_CACHE_NAMES) {
                clearOneLocalCache(name);
            }
            log.info("已清空全部 Caffeine 本地缓存");
        } else {
            clearOneLocalCache(cacheName);
        }
    }

    private void clearOneLocalCache(String cacheName) {
        try {
            Cache<?, ?> cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                // 通过 unwrap 获取底层 Caffeine Cache 实例并清空
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine = cache.unwrap(com.github.benmanes.caffeine.cache.Cache.class);
                if (caffeine != null) {
                    caffeine.invalidateAll();
                    log.info("Caffeine 本地缓存 [{}] 已清空", cacheName);
                }
            }
        } catch (Exception e) {
            log.warn("清除 Caffeine 本地缓存 [{}] 失败: {}", cacheName, e.getMessage());
        }
    }

    // ==================== Redis 操作 ====================

    /** 扫描 Redis key 列表 */
    public List<RedisKeyVO> getRedisKeys(String pattern) {
        String matchPattern = (pattern != null && !pattern.isBlank())
                ? pattern : "*";

        return redisTemplate.execute((RedisCallback<List<RedisKeyVO>>) connection -> {
            List<RedisKeyVO> records = new ArrayList<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match(matchPattern)
                    .count(MAX_SCAN_KEYS)
                    .build());

            cursor.forEachRemaining(keyBytes -> {
                if (records.size() >= MAX_SCAN_KEYS) {
                    return;
                }
                String fullKey = new String(keyBytes, StandardCharsets.UTF_8);
                RedisKeyVO vo = new RedisKeyVO();
                vo.setKey(fullKey);
                try {
                    vo.setType(connection.type(keyBytes).code());
                } catch (Exception e) {
                    vo.setType("unknown");
                }
                Long ttl = connection.ttl(keyBytes);
                vo.setTtl(ttl != null ? ttl : -2);
                records.add(vo);
            });

            try {
                cursor.close();
            } catch (Exception e) {
                log.warn("关闭 Redis cursor 失败: {}", e.getMessage());
            }
            return records;
        });
    }

    /** 批量删除 Redis key */
    @BizLog("删除Redis缓存")
    public long deleteRedisKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        Long deleted = redisTemplate.delete(keys);
        log.info("已删除 {} 个 Redis key", deleted != null ? deleted : 0);
        return deleted != null ? deleted : 0;
    }

    /** 按前缀批量清除 Redis key */
    @BizLog("按前缀清理Redis缓存")
    public long clearRedisByPrefix(String prefix) {
        String matchPattern = (prefix != null && !prefix.isBlank())
                ? prefix + "*"
                : "*";

        List<String> keysToDelete = new ArrayList<>();

        redisTemplate.execute((RedisCallback<Void>) connection -> {
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match(matchPattern)
                    .count(MAX_SCAN_KEYS)
                    .build());

            cursor.forEachRemaining(keyBytes -> {
                keysToDelete.add(new String(keyBytes, StandardCharsets.UTF_8));
            });

            try {
                cursor.close();
            } catch (Exception e) {
                log.warn("关闭 Redis cursor 失败: {}", e.getMessage());
            }
            return null;
        });

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("已按前缀 [{}] 清除 {} 个 Redis key", matchPattern, keysToDelete.size());
        }
        return keysToDelete.size();
    }
}
