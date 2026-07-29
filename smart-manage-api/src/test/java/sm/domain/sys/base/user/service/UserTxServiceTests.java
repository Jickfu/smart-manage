package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sm.domain.sys.base.user.constant.UserThemeColor;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserTxServiceTests {

    @Test
    void newUserUsesDefaultThemeAndMustChangeInitialPassword() {
        UserMapper userMapper = mock(UserMapper.class);
        UserTxService service = new UserTxService(userMapper, mock(UserRoleMapper.class));
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
