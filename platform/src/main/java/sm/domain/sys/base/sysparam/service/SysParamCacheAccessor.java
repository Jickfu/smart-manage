package sm.domain.sys.base.sysparam.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.domain.sys.base.sysparam.mapper.SysParamMapper;
import sm.domain.sys.base.sysparam.model.entity.SysParamEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统参数缓存访问器。
 *
 * <p>缓存方法必须位于独立 Spring Bean，确保系统参数服务调用时经过 JetCache 代理。</p>
 * <p>本类只负责缓存读取；保存和删除后的统一失效由 {@link SysParamService} 负责。</p>
 */
@Component
@RequiredArgsConstructor
class SysParamCacheAccessor {
    private final SysParamMapper mapper;

    /** 全量获取 number → value 映射（Redis 远程缓存）。 */
    @Cached(cacheType = CacheType.REMOTE, name = BaseCacheName.SYS_PARAM,
            key = "T(sm.domain.sys.base.common.constant.BaseCacheName).ALL_KEY",
            expire = 30, timeUnit = TimeUnit.MINUTES)
    public Map<String, String> getAll() {
        List<SysParamEntity> entityList = mapper.selectList(null);
        Map<String, String> parameters = new HashMap<>();
        for (SysParamEntity entity : entityList) {
            parameters.put(entity.getNumber(), entity.getValue());
        }
        return parameters;
    }
}
