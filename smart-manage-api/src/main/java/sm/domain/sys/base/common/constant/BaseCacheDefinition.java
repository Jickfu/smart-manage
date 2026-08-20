package sm.domain.sys.base.common.constant;

import com.alicp.jetcache.anno.CacheType;

/** 系统管理应用缓存的唯一受控定义。 */
public record BaseCacheDefinition(String name, String displayName, CacheType cacheType,
                                  String description, long expireSeconds, boolean sensitiveValue) {
}
