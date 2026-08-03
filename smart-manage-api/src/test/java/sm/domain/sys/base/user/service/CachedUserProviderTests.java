package sm.domain.sys.base.user.service;

import com.alicp.jetcache.anno.Cached;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CachedUserProviderTests {

	@Test
	void cachedUserProviderIsTheOnlyCacheDefinitionForPublicUserLookup() throws NoSuchMethodException {
		Cached cached = CachedUserProvider.class.getMethod("requireUser", Long.class)
				.getAnnotation(Cached.class);
		assertNotNull(cached);
		assertNull(UserService.class.getMethod("requireUser", Long.class).getAnnotation(Cached.class));
	}

	@Test
	void requireUserReturnsPersistedUser() {
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity user = new UserEntity();
		when(userMapper.selectById(1L)).thenReturn(user);

		assertSame(user, new CachedUserProvider(userMapper).requireUser(1L));
	}

	@Test
	void requireUserRejectsMissingUser() {
		UserMapper userMapper = mock(UserMapper.class);

		assertThrows(BizException.class, () -> new CachedUserProvider(userMapper).requireUser(1L));
	}
}
