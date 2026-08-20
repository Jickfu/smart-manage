package sm.domain.sys.base.user.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.concurrent.TimeUnit;

/** 独立用户缓存访问器，避免 UserService 内部调用绕过缓存代理。 */
@Component
@RequiredArgsConstructor
class UserCacheAccessor {
    private final UserMapper mapper;

    @Cached(cacheType = CacheType.REMOTE, name = BaseCacheName.USER_INFO,
            key = "#id", expire = 1, timeUnit = TimeUnit.HOURS)
    public UserEntity requireUser(Long id) {
        UserEntity entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        return entity;
    }
}
