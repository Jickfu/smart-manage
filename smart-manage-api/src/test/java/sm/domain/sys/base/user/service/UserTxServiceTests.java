package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import sm.domain.sys.base.user.constant.UserThemeColor;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.service.OrgReferenceService;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.form.UserAssignmentForm;
import sm.domain.sys.base.user.model.form.UserOrganizationRoleForm;
import sm.domain.sys.base.user.model.form.UserRoleAssignmentSaveForm;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.system.security.context.CurrentUserContext;
import sm.system.helper.Argon2Helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

class UserTxServiceTests {

	@Test
	void currentPasswordMustMatchBeforePasswordCanBeChanged() {
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity existing = new UserEntity();
		existing.setId(1L);
		existing.setPassword("encoded-password");
		when(userMapper.selectById(1L)).thenReturn(existing);
		UserTxService service = new UserTxService(
				userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));

		try (MockedStatic<Argon2Helper> argon2Helper = mockStatic(Argon2Helper.class)) {
			argon2Helper.when(() -> Argon2Helper.verify("encoded-password", "wrong-password"))
					.thenReturn(false);

			assertThrows(sm.system.exception.BizException.class,
					() -> service.updateCurrentPassword(1L, "wrong-password", "new-password"));
		}
	}

	@Test
	void currentPasswordMustMatchBeforeContactCanBeChanged() {
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity existing = new UserEntity();
		existing.setId(1L);
		existing.setPassword("encoded-password");
		when(userMapper.selectById(1L)).thenReturn(existing);
		UserTxService service = new UserTxService(
				userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));

		try (MockedStatic<Argon2Helper> argon2Helper = mockStatic(Argon2Helper.class)) {
			argon2Helper.when(() -> Argon2Helper.verify("encoded-password", "wrong-password"))
					.thenReturn(false);

			assertThrows(sm.system.exception.BizException.class,
					() -> service.updateCurrentContact(1L, "wrong-password", "EMAIL", "user@example.com"));
		}
	}

	@Test
	void currentProfileOnlyUpdatesSelfMaintainableFields() {
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity existing = new UserEntity();
		existing.setId(1L);
		existing.setUsername("unchanged-user");
		when(userMapper.selectById(1L)).thenReturn(existing);
		when(userMapper.updateById(existing)).thenReturn(1);
		UserTxService service = new UserTxService(
				userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));

		service.updateCurrentProfile(1L, " 新姓名 ", null, null, 20L);

		assertEquals("新姓名", existing.getName());
		assertEquals(20L, existing.getAvatarAttachmentId());
		assertEquals("unchanged-user", existing.getUsername());
		verify(userMapper).updateById(existing);
	}

	@Test
	void assignmentsRequireExactlyOnePrimaryPosition() {
		UserMapper userMapper = mock(UserMapper.class);
		when(userMapper.selectCount(any())).thenReturn(0L);
		UserTxService service = new UserTxService(
				userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));
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
				new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));
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
				new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));
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
                new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));
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
		assertFalse(saved.getEnabled());
    }

	@Test
	void enableRejectsUserWithoutPrimaryOrganization() {
		UserTxService service = new UserTxService(
				mock(UserMapper.class), mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));

		assertThrows(sm.system.exception.BizException.class,
				() -> service.updateEnabled(List.of(10L), true));
	}

	@Test
	void roleAssignmentReplacesAllExplicitOrganizationRoles() {
		UserMapper userMapper = mock(UserMapper.class);
		UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
		UserAssignmentMapper userAssignmentMapper = mock(UserAssignmentMapper.class);
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		when(userMapper.selectById(10L)).thenReturn(new UserEntity());
		UserAssignmentEntity firstAssignment = new UserAssignmentEntity();
		firstAssignment.setOrgId(20L);
		UserAssignmentEntity secondAssignment = new UserAssignmentEntity();
		secondAssignment.setOrgId(21L);
		when(userAssignmentMapper.selectList(any())).thenReturn(List.of(firstAssignment, secondAssignment));
		when(userRoleMapper.selectExistingRoleIds(any())).thenReturn(List.of(30L, 31L, 32L));
		when(userRoleMapper.insert(any(UserRoleEntity.class))).thenReturn(1);
		UserTxService service = new UserTxService(
				userMapper, userRoleMapper, userAssignmentMapper,
				new OrgReferenceService(mock(OrgMapper.class)), currentUserContext);
		UserRoleAssignmentSaveForm form = new UserRoleAssignmentSaveForm();
		form.setUserId(10L);
		form.setAssignments(List.of(organizationRoles(20L, 30L, 31L), organizationRoles(21L, 32L)));

		service.saveRoleAssignment(form);

		ArgumentCaptor<UserRoleEntity> relationCaptor = ArgumentCaptor.forClass(UserRoleEntity.class);
		verify(userRoleMapper, org.mockito.Mockito.times(3)).insert(relationCaptor.capture());
		assertEquals(List.of(20L, 20L, 21L), relationCaptor.getAllValues().stream()
				.map(UserRoleEntity::getOrgId).toList());
		verify(currentUserContext, never()).getOrgId();
	}

	@Test
	void roleAssignmentRejectsOrganizationOutsideUserAssignments() {
		UserMapper userMapper = mock(UserMapper.class);
		UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
		UserAssignmentMapper userAssignmentMapper = mock(UserAssignmentMapper.class);
		when(userMapper.selectById(10L)).thenReturn(new UserEntity());
		UserAssignmentEntity assignment = new UserAssignmentEntity();
		assignment.setOrgId(20L);
		when(userAssignmentMapper.selectList(any())).thenReturn(List.of(assignment));
		UserTxService service = new UserTxService(
				userMapper, userRoleMapper, userAssignmentMapper,
				new OrgReferenceService(mock(OrgMapper.class)), mock(CurrentUserContext.class));
		UserRoleAssignmentSaveForm form = new UserRoleAssignmentSaveForm();
		form.setUserId(10L);
		form.setAssignments(List.of(organizationRoles(99L, 30L)));

		assertThrows(sm.system.exception.BizException.class, () -> service.saveRoleAssignment(form));
		verify(userRoleMapper, never()).insert(any(UserRoleEntity.class));
	}

	@Test
	void removingAssignmentAlsoRemovesRolesInThatOrganization() {
		UserMapper userMapper = mock(UserMapper.class);
		UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
		UserAssignmentMapper userAssignmentMapper = mock(UserAssignmentMapper.class);
		OrgMapper orgMapper = mock(OrgMapper.class);
		UserEntity existing = new UserEntity();
		existing.setId(10L);
		existing.setUsername("assignment-user");
		existing.setVersion(0);
		when(userMapper.selectById(10L)).thenReturn(existing);
		when(userMapper.selectCount(any())).thenReturn(0L);
		when(userMapper.updateById(any(UserEntity.class))).thenReturn(1);
		UserAssignmentEntity removedAssignment = new UserAssignmentEntity();
		removedAssignment.setOrgId(20L);
		when(userAssignmentMapper.selectList(any())).thenReturn(List.of(removedAssignment));
		UserTxService service = new UserTxService(
				userMapper, userRoleMapper, userAssignmentMapper, new OrgReferenceService(orgMapper),
				mock(CurrentUserContext.class));
		UserSaveForm form = new UserSaveForm();
		form.setId(10L);
		form.setVersion(0);
		form.setUsername("assignment-user");
		form.setName("任职用户");
		form.setNumber("U-ASG");
		form.setAssignments(List.of());

		service.save(form);

		verify(userRoleMapper).delete(any());
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

	private static UserOrganizationRoleForm organizationRoles(Long orgId, Long... roleIds) {
		UserOrganizationRoleForm assignment = new UserOrganizationRoleForm();
		assignment.setOrgId(orgId);
		assignment.setRoleIds(List.of(roleIds));
		return assignment;
	}
}
