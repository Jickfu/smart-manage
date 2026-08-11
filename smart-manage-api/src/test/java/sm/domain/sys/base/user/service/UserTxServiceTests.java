package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sm.domain.sys.base.user.constant.UserThemeColor;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.form.UserAssignmentForm;
import sm.domain.sys.base.common.helper.CurrentUserContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserTxServiceTests {

	@Test
	void assignmentsRequireExactlyOnePrimaryPosition() {
		UserMapper userMapper = mock(UserMapper.class);
		when(userMapper.selectCount(any())).thenReturn(0L);
		UserTxService service = new UserTxService(
				userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				mock(OrgMapper.class), mock(CurrentUserContext.class));
		UserSaveForm form = newUserForm();
		form.setAssignments(List.of(assignment(10L, false), assignment(11L, false)));

		assertThrows(sm.system.exception.BizException.class, () -> service.save(form));
	}

	@Test
	void sameOrganizationCannotAppearTwice() {
		UserMapper userMapper = mock(UserMapper.class);
		when(userMapper.selectCount(any())).thenReturn(0L);
		UserTxService service = new UserTxService(
				userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				mock(OrgMapper.class), mock(CurrentUserContext.class));
		UserSaveForm form = newUserForm();
		form.setAssignments(List.of(assignment(10L, true), assignment(10L, false)));

		assertThrows(sm.system.exception.BizException.class, () -> service.save(form));
	}

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
				userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				mock(OrgMapper.class), mock(CurrentUserContext.class));
		UserSaveForm form = new UserSaveForm();
		form.setId(1L);
		form.setVersion(0);
		form.setUsername("renamed-user");
		form.setName("用户");
		form.setNumber("U001");

		assertThrows(sm.system.exception.BizException.class, () -> service.save(form));
	}

	@Test
	void newUserUsesDefaultThemeAndMustChangeInitialPassword() {
        UserMapper userMapper = mock(UserMapper.class);
        UserTxService service = new UserTxService(
                userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
                mock(OrgMapper.class), mock(CurrentUserContext.class));
        UserSaveForm form = new UserSaveForm();
        form.setUsername("new-user");
        form.setPassword("InitialPassword1!");
        form.setName("新用户");
        form.setNumber("U002");
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(UserEntity.class))).thenReturn(1);

        service.save(form);

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).insert(entityCaptor.capture());
        UserEntity saved = entityCaptor.getValue();
        assertEquals(UserThemeColor.DEFAULT, saved.getThemeColor());
        assertTrue(saved.getPasswordReset());
    }

	private static UserSaveForm newUserForm() {
		UserSaveForm form = new UserSaveForm();
		form.setUsername("assignment-user");
		form.setPassword("InitialPassword1!");
		form.setName("任职用户");
		form.setNumber("U-ASG");
		return form;
	}

	private static UserAssignmentForm assignment(Long orgId, boolean primary) {
		UserAssignmentForm assignment = new UserAssignmentForm();
		assignment.setOrgId(orgId);
		assignment.setPosition("工程师");
		assignment.setIsPrimary(primary);
		return assignment;
	}
}
