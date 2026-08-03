package sm.domain.sys.base.common.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.service.CachedUserProvider;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserServiceTests {

	@Test
	void administratorIdentityUsesCurrentPersistedUsername() {
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		CachedUserProvider cachedUserProvider = mock(CachedUserProvider.class);
		UserEntity user = new UserEntity();
		user.setUsername("administrator");
		when(currentUserContext.isLogin()).thenReturn(true);
		when(currentUserContext.getUserId()).thenReturn(1L);
		when(cachedUserProvider.requireUser(1L)).thenReturn(user);
		CurrentUserService service = new CurrentUserService(currentUserContext, cachedUserProvider);

		assertDoesNotThrow(service::checkAdministrator);
	}

	@Test
	void administratorIdentityIsCaseSensitive() {
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		CachedUserProvider cachedUserProvider = mock(CachedUserProvider.class);
		UserEntity user = new UserEntity();
		user.setUsername("Administrator");
		when(currentUserContext.isLogin()).thenReturn(true);
		when(currentUserContext.getUserId()).thenReturn(1L);
		when(cachedUserProvider.requireUser(1L)).thenReturn(user);
		CurrentUserService service = new CurrentUserService(currentUserContext, cachedUserProvider);

		assertFalse(service.isAdministrator());
		assertThrows(BizException.class, service::checkAdministrator);
	}

	@Test
	void anonymousUserCannotPassAdministratorCheck() {
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		CurrentUserService service = new CurrentUserService(
				currentUserContext, mock(CachedUserProvider.class));

		assertFalse(service.isAdministrator());
		assertThrows(BizException.class, service::checkAdministrator);
	}
}
