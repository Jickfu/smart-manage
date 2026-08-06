package sm.domain.sys.base.common.helper;

import cn.dev33.satoken.stp.StpUtil;
import com.alicp.jetcache.Cache;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.system.helper.CacheHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorizationStateHelperTests {
    private final CacheHelper cacheHelper = mock(CacheHelper.class);
    private final UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
    private final Cache<Long, Object> cache = mock(Cache.class);
    private final AuthorizationStateHelper helper = new AuthorizationStateHelper(cacheHelper, userRoleMapper);

    @Test
    void refreshUsersDoesNotTerminateSessions() {
        when(cacheHelper.<Long, Object>getCache(anyString(), any())).thenReturn(cache);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            helper.refreshUsers(List.of(1L, 1L));

            stpUtil.verify(() -> StpUtil.logout(1L), never());
        }
        verify(cache).remove(1L);
    }

    @Test
    void terminateUsersRefreshesCacheAndLogsOutEveryDistinctUser() {
        when(cacheHelper.<Long, Object>getCache(anyString(), any())).thenReturn(cache);
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            helper.terminateUsers(List.of(1L, 1L, 2L));

            stpUtil.verify(() -> StpUtil.logout(1L));
            stpUtil.verify(() -> StpUtil.logout(2L));
        }
        verify(cache).remove(1L);
        verify(cache).remove(2L);
    }
}
