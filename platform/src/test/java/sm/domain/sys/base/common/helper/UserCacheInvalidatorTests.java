package sm.domain.sys.base.common.helper;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import org.junit.jupiter.api.Test;
import sm.system.helper.CacheHelper;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserCacheInvalidatorTests {
    @SuppressWarnings("unchecked")
    private final Cache<Long, Object> cache = mock(Cache.class);
    private final CacheHelper caches = mock(CacheHelper.class);
    private final UserCacheInvalidator invalidator = new UserCacheInvalidator(caches);

    private void configure() {
        when(caches.<Long, Object>getCache(anyString(), any(), anyLong())).thenReturn(cache);
    }

    @Test
    void failedDeletionIsNotSilentSuccess() {
        configure();
        when(cache.REMOVE(1L)).thenReturn(CacheResult.FAIL_WITHOUT_MSG);
        assertThrows(IllegalStateException.class, () -> invalidator.refreshUsers(List.of(1L)));
    }

    @Test
    void missingKeyIsAnIdempotentSuccess() {
        configure();
        when(cache.REMOVE(1L)).thenReturn(new CacheResult(CacheResultCode.NOT_EXISTS, null));
        assertDoesNotThrow(() -> invalidator.refreshUsers(List.of(1L, 1L)));
        verify(cache).REMOVE(1L);
    }

    @Test
    void displayCacheFailureDoesNotInvalidateCommittedCredentialResultOrSkipOtherUsers() {
        configure();
        when(cache.REMOVE(1L)).thenThrow(new IllegalStateException("injected"));
        when(cache.REMOVE(2L)).thenReturn(CacheResult.SUCCESS_WITHOUT_MSG);
        assertDoesNotThrow(() -> invalidator.tryRefreshUsers(List.of(1L, 2L)));
        verify(cache).REMOVE(2L);
    }
}
