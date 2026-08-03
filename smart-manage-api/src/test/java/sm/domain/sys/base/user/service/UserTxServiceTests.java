package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sm.domain.sys.base.user.constant.UserThemeColor;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.common.helper.CurrentUserContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserTxServiceTests {

	@Test
	void existingLoginUsernameCannotBeChanged() {
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity existing = new UserEntity();
		existing.setId(1L);
		existing.setUsername("original-user");
		existing.setVersion(0);
		when(userMapper.selectCount(any())).thenReturn(0L);
		when(userMapper.selectById(1L)).thenReturn(existing);
		UserTxService service = new UserTxService(
				userMapper, mock(UserRoleMapper.class), mock(CurrentUserContext.class));
		UserSaveForm form = new UserSaveForm();
		form.setId(1L);
		form.setVersion(0);
		form.setUsername("renamed-user");

		assertThrows(sm.system.exception.BizException.class, () -> service.save(form));
	}

	@Test
    void newUserUsesDefaultThemeAndMustChangeInitialPassword() {
        UserMapper userMapper = mock(UserMapper.class);
        UserTxService service = new UserTxService(
                userMapper, mock(UserRoleMapper.class), mock(CurrentUserContext.class));
        UserSaveForm form = new UserSaveForm();
        form.setUsername("new-user");
        form.setPassword("InitialPassword1!");
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(UserEntity.class))).thenReturn(1);

        service.save(form);

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).insert(entityCaptor.capture());
        UserEntity saved = entityCaptor.getValue();
        assertEquals(UserThemeColor.DEFAULT, saved.getThemeColor());
        assertTrue(saved.getPasswordReset());
    }
}
