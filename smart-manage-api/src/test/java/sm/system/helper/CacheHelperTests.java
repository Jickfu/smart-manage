package sm.system.helper;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheHelperTests {

	@Test
	@SuppressWarnings("unchecked")
	void cacheIdentityIncludesNameAndCacheType() {
		CacheManager cacheManager = mock(CacheManager.class);
		Cache<Object, Object> localCache = mock(Cache.class);
		Cache<Object, Object> remoteCache = mock(Cache.class);
		when(cacheManager.getOrCreateCache(any(QuickConfig.class))).thenReturn(localCache, remoteCache);
		CacheHelper cacheHelper = new CacheHelper(cacheManager);

		Cache<Object, Object> firstLocal = cacheHelper.getCache("same-name", CacheType.LOCAL);
		Cache<Object, Object> secondLocal = cacheHelper.getCache("same-name", CacheType.LOCAL);
		Cache<Object, Object> remote = cacheHelper.getCache("same-name", CacheType.REMOTE);

		assertSame(firstLocal, secondLocal);
		assertNotSame(firstLocal, remote);
		verify(cacheManager, times(2)).getOrCreateCache(any(QuickConfig.class));
	}
}
