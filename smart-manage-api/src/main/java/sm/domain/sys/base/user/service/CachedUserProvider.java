package sm.domain.sys.base.user.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.concurrent.TimeUnit;

/** 用户实体查询及缓存的单一权威来源。 */
@Component
@RequiredArgsConstructor
public class CachedUserProvider {
	private final UserMapper userMapper;

	@Cached(cacheType = CacheType.REMOTE, name = CacheConstant.USER_INFO,
			key = "#userId", expire = 1, timeUnit = TimeUnit.HOURS)
	public UserEntity requireUser(Long userId) {
		UserEntity user = userMapper.selectById(userId);
		if (user == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
		}
		return user;
	}
}
