package sm.domain.sys.monitor.cache.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CacheServiceTests {

    private final CacheHelper cacheHelper = mock(CacheHelper.class);
    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final CacheService service = new CacheService(cacheHelper, redisTemplate, currentUserContext);

    @Test
    void clearMustRejectCachesOutsideManagedCatalog() {
        assertThrows(BizException.class, () -> service.clear("unmanaged-cache"));
        verify(currentUserContext).checkAdministrator();
    }
}
