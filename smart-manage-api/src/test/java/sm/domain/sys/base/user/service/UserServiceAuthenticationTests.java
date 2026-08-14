package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.menu.service.MenuService;
import sm.domain.sys.base.permission.service.PermissionService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.system.helper.Argon2Helper;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class UserServiceAuthenticationTests {

	@Test
	void switchCurrentOrganizationOnlyAcceptsAvailableAssignment() {
		UserAssignmentMapper assignmentMapper = mock(UserAssignmentMapper.class);
		UserAssignmentEntity assignment = new UserAssignmentEntity();
		assignment.setUserId(1L);
		assignment.setOrgId(20L);
		when(assignmentMapper.selectOne(any())).thenReturn(assignment);
		OrgMapper orgMapper = mock(OrgMapper.class);
		OrgEntity organization = new OrgEntity();
		organization.setId(20L);
		organization.setEnabled(true);
		organization.setArchived(false);
		when(orgMapper.selectById(20L)).thenReturn(organization);
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		when(currentUserContext.getUserId()).thenReturn(1L);
		UserService service = createService(assignmentMapper, orgMapper, currentUserContext);

		service.switchCurrentOrganization(20L);

		verify(currentUserContext).setOrgId(20L);
	}

	@Test
	void switchCurrentOrganizationRejectsOrganizationOutsideAssignments() {
		UserAssignmentMapper assignmentMapper = mock(UserAssignmentMapper.class);
		when(assignmentMapper.selectOne(any())).thenReturn(null);
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		when(currentUserContext.getUserId()).thenReturn(1L);
		UserService service = createService(
				assignmentMapper, mock(OrgMapper.class), currentUserContext);

		assertThrows(BizException.class, () -> service.switchCurrentOrganization(20L));

		verify(currentUserContext, never()).setOrgId(any());
	}

	@Test
	void administratorLoginIdentityIsCaseSensitive() {
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("Administrator");
		user.setPassword("encoded-password");
		user.setEnabled(true);
		when(userMapper.selectOne(any())).thenReturn(user);
		UserAssignmentMapper assignmentMapper = mock(UserAssignmentMapper.class);
		UserAssignmentEntity primaryAssignment = new UserAssignmentEntity();
		primaryAssignment.setOrgId(10L);
		when(assignmentMapper.selectOne(any())).thenReturn(primaryAssignment);
		OrgMapper orgMapper = mock(OrgMapper.class);
		OrgEntity organization = new OrgEntity();
		organization.setId(10L);
		organization.setEnabled(true);
		organization.setArchived(false);
		when(orgMapper.selectById(10L)).thenReturn(organization);
		UserService service = new UserService(
				userMapper,
				mock(UserRoleMapper.class),
				assignmentMapper,
				orgMapper,
				mock(AttachmentService.class),
				mock(UserTxService.class),
				mock(MenuService.class),
				mock(PermissionService.class),
				mock(AuthorizationStateHelper.class),
				mock(UserConverter.class),
				mock(CurrentUserContext.class));

		try (MockedStatic<Argon2Helper> argon2Helper = mockStatic(Argon2Helper.class)) {
			argon2Helper.when(() -> Argon2Helper.verify("encoded-password", "password"))
					.thenReturn(true);

			UserAuthentication authentication = service.authenticate("Administrator", "password");

			assertTrue(authentication.successful());
			assertFalse(authentication.administrator());
		}
	}

	private UserService createService(UserAssignmentMapper assignmentMapper, OrgMapper orgMapper,
			CurrentUserContext currentUserContext) {
		return new UserService(
				mock(UserMapper.class),
				mock(UserRoleMapper.class),
				assignmentMapper,
				orgMapper,
				mock(AttachmentService.class),
				mock(UserTxService.class),
				mock(MenuService.class),
				mock(PermissionService.class),
				mock(AuthorizationStateHelper.class),
				mock(UserConverter.class),
				currentUserContext);
	}
}
