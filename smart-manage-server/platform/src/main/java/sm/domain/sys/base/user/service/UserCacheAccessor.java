package sm.domain.sys.base.user.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.UserCacheSnapshot;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.concurrent.TimeUnit;

/** 稳定的用户缓存摘要读取边界，供认证和审计等调用方使用。 */
@Component
@RequiredArgsConstructor
public class UserCacheAccessor {
    private final UserMapper mapper;

    @Cached(cacheType = CacheType.REMOTE, name = BaseCacheName.USER_INFO,
            key = "#id", expire = 1, timeUnit = TimeUnit.HOURS)
    public UserCacheSnapshot requireUser(Long id) {
        UserCacheSnapshot snapshot = mapper.selectCacheSnapshotById(id);
        if (snapshot == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        return snapshot;
    }
}
