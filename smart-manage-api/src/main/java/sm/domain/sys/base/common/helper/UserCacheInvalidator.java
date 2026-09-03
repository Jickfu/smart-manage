package sm.domain.sys.base.common.helper;

import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.anno.CacheType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.system.helper.CacheHelper;

import java.util.Collection;
import java.util.LinkedHashSet;

/** 只清理非认证用户展示缓存；授权和会话有效性不依赖本组件。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCacheInvalidator {
    private final CacheHelper cacheHelper;

    public void refreshUsers(Collection<Long> userIds) {
        for (Long userId : new LinkedHashSet<>(userIds)) {
            var result = cacheHelper.<Long, Object>getCache(BaseCacheName.USER_INFO, CacheType.REMOTE, 3600)
                    .REMOVE(userId);
            if (result == null || (result.getResultCode() != CacheResultCode.SUCCESS
                    && result.getResultCode() != CacheResultCode.NOT_EXISTS)) {
                throw new IllegalStateException("用户展示缓存清理失败");
            }
        }
    }

    /** 安全写入已经提交且旧会话已由数据库代际失效，不将展示缓存故障误报为改密失败。 */
    public void tryRefreshUsers(Collection<Long> userIds) {
        for (Long userId : new LinkedHashSet<>(userIds)) {
            try {
                refreshUsers(java.util.List.of(userId));
            } catch (RuntimeException exception) {
                log.warn("用户安全事件已提交，展示缓存清理失败，等待 TTL 重建: userId={}", userId);
            }
        }
    }
}
