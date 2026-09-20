package sm.domain.sys.base.common.constant;

import com.alicp.jetcache.anno.CacheType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 业务声明、主动失效和运维展示共同使用的缓存目录。 */
public final class BaseCacheCatalog {
    public static final BaseCacheDefinition USER_INFO = new BaseCacheDefinition(
            BaseCacheName.USER_INFO, "用户信息", CacheType.REMOTE, "日志与用户查询按 ID 读取的非认证快照", 3600, true);
    public static final BaseCacheDefinition SYS_PARAM = new BaseCacheDefinition(
            BaseCacheName.SYS_PARAM, "系统参数", CacheType.REMOTE, "系统参数全量快照", 1800, false);
    public static final BaseCacheDefinition BASIC_DATA_OPTIONS = new BaseCacheDefinition(
            BaseCacheName.BASIC_DATA_OPTIONS, "基础数据选项", CacheType.REMOTE, "按分类编码缓存的下拉选项", 1800, false);

    public static final List<BaseCacheDefinition> ALL = List.of(
            USER_INFO, SYS_PARAM, BASIC_DATA_OPTIONS);
    public static final Map<String, BaseCacheDefinition> BY_NAME = byName();

    private BaseCacheCatalog() {
    }

    private static Map<String, BaseCacheDefinition> byName() {
        Map<String, BaseCacheDefinition> definitions = new LinkedHashMap<>();
        ALL.forEach(definition -> definitions.put(definition.name(), definition));
        return Map.copyOf(definitions);
    }
}
