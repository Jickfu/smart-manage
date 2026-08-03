package sm.domain.sys.base.common.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
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
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity user = new UserEntity();
		user.setUsername("administrator");
		when(currentUserContext.isLogin()).thenReturn(true);
		when(currentUserContext.getUserId()).thenReturn(1L);
		when(userMapper.selectById(1L)).thenReturn(user);
		CurrentUserService service = new CurrentUserService(currentUserContext, userMapper);

		assertDoesNotThrow(service::checkAdministrator);
	}

	@Test
	void anonymousUserCannotPassAdministratorCheck() {
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		CurrentUserService service = new CurrentUserService(currentUserContext, mock(UserMapper.class));

		assertFalse(service.isAdministrator());
		assertThrows(BizException.class, service::checkAdministrator);
	}
}
