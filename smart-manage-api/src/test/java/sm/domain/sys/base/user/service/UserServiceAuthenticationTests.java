package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.system.security.context.CurrentUserContext;
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
import sm.domain.sys.base.user.model.form.UserOrganizationRoleForm;
import sm.domain.sys.base.user.model.form.UserRoleAssignmentSaveForm;
import sm.system.helper.Argon2Helper;
import sm.system.exception.BizException;
import sm.system.security.CsrfTokenManager;
import sm.system.auth.SessionTerminationReason;
import sm.system.response.ResultEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import java.util.List;

class UserServiceAuthenticationTests {

	@Test
	void deletingUserTerminatesSessionsOnlyAfterDeleteSucceeds() {
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity user = new UserEntity();
		user.setId(10L);
		when(userMapper.selectById(10L)).thenReturn(user);
		UserTxService txService = mock(UserTxService.class);
		AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
		UserService service = createService(userMapper, txService, authorizationStateHelper,
				mock(CurrentUserContext.class), mock(UserConverter.class));

		service.deleteById(10L);

		verify(txService).deleteById(10L);
		verify(authorizationStateHelper).terminateUsers(
				List.of(10L), SessionTerminationReason.ACCOUNT_DELETED);
	}

	@Test
	void failedDeleteDoesNotTerminateSessions() {
		UserTxService txService = mock(UserTxService.class);
		doThrow(new BizException(ResultEnum.DATA_CONFLICT)).when(txService).deleteById(10L);
		AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
		UserService service = createService(mock(UserMapper.class), txService, authorizationStateHelper,
				mock(CurrentUserContext.class), mock(UserConverter.class));

		assertThrows(BizException.class, () -> service.deleteById(10L));

		verify(authorizationStateHelper, never()).terminateUsers(any(), any());
	}

	@Test
	void disablingUsersTerminatesSessionsOnlyAfterUpdateSucceeds() {
		UserTxService txService = mock(UserTxService.class);
		AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
		UserService service = createService(mock(UserMapper.class), txService, authorizationStateHelper,
				mock(CurrentUserContext.class), mock(UserConverter.class));

		service.disable(List.of(10L));

		verify(txService).updateEnabled(List.of(10L), false);
		verify(authorizationStateHelper).terminateUsers(
				List.of(10L), SessionTerminationReason.ACCOUNT_DISABLED);
	}

	@Test
	void failedDisableDoesNotTerminateSessions() {
		UserTxService txService = mock(UserTxService.class);
		doThrow(new BizException(ResultEnum.DATA_CONFLICT))
				.when(txService).updateEnabled(List.of(10L), false);
		AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
		UserService service = createService(mock(UserMapper.class), txService, authorizationStateHelper,
				mock(CurrentUserContext.class), mock(UserConverter.class));

		assertThrows(BizException.class, () -> service.disable(List.of(10L)));

		verify(authorizationStateHelper, never()).terminateUsers(any(), any());
	}

	@Test
	void missingCurrentUserTerminatesSessionAndReturnsUnauthorized() {
		UserMapper userMapper = mock(UserMapper.class);
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		when(currentUserContext.getUserId()).thenReturn(10L);
		AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
		UserService service = createService(userMapper, mock(UserTxService.class), authorizationStateHelper,
				currentUserContext, mock(UserConverter.class));

		BizException exception = assertThrows(BizException.class, service::current);

		assertEquals(ResultEnum.UNAUTHORIZED.getCode(), exception.getCode());
		verify(authorizationStateHelper).terminateUsers(
				List.of(10L), SessionTerminationReason.ACCOUNT_DELETED);
	}

	@Test
	void disabledCurrentUserTerminatesSessionAndReturnsUnauthorized() {
		UserMapper userMapper = mock(UserMapper.class);
		UserEntity user = new UserEntity();
		user.setId(10L);
		user.setEnabled(false);
		when(userMapper.selectById(10L)).thenReturn(user);
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		when(currentUserContext.getUserId()).thenReturn(10L);
		AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
		UserService service = createService(userMapper, mock(UserTxService.class), authorizationStateHelper,
				currentUserContext, mock(UserConverter.class));

		BizException exception = assertThrows(BizException.class, service::current);

		assertEquals(ResultEnum.UNAUTHORIZED.getCode(), exception.getCode());
		verify(authorizationStateHelper).terminateUsers(
				List.of(10L), SessionTerminationReason.ACCOUNT_DISABLED);
	}

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
		UserCacheAccessor userCacheAccessor = mock(UserCacheAccessor.class);
		UserService service = new UserService(
				userMapper,
				mock(UserRoleMapper.class),
				assignmentMapper,
				orgMapper,
				mock(AttachmentService.class),
				mock(UserTxService.class),
				mock(PermissionService.class),
				mock(AuthorizationStateHelper.class),
				userCacheAccessor,
				mock(UserConverter.class),
				mock(CurrentUserContext.class),
				mock(CsrfTokenManager.class));

		try (MockedStatic<Argon2Helper> argon2Helper = mockStatic(Argon2Helper.class)) {
			argon2Helper.when(() -> Argon2Helper.verify("encoded-password", "password"))
					.thenReturn(true);

			UserAuthentication authentication = service.authenticate("Administrator", "password");

			assertTrue(authentication.successful());
			assertFalse(authentication.administrator());
			verify(userCacheAccessor, never()).requireUser(any());
		}
	}

	@Test
	void savingRoleAssignmentRefreshesPreviousAndSubmittedOrganizationCaches() {
		UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
		when(userRoleMapper.selectOrgIdsByUserId(10L)).thenReturn(List.of(20L));
		UserTxService txService = mock(UserTxService.class);
		AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
		UserService service = new UserService(
				mock(UserMapper.class), userRoleMapper, mock(UserAssignmentMapper.class),
				mock(OrgMapper.class), mock(AttachmentService.class), txService,
				mock(PermissionService.class), authorizationStateHelper,
				mock(UserCacheAccessor.class), mock(UserConverter.class),
				mock(CurrentUserContext.class), mock(CsrfTokenManager.class));
		UserOrganizationRoleForm organization = new UserOrganizationRoleForm();
		organization.setOrgId(21L);
		organization.setRoleIds(List.of(30L));
		UserRoleAssignmentSaveForm form = new UserRoleAssignmentSaveForm();
		form.setUserId(10L);
		form.setAssignments(List.of(organization));

		service.saveRoleAssignment(form);

		verify(txService).saveRoleAssignment(form);
		verify(authorizationStateHelper).refreshUserAuthorization(10L, 20L);
		verify(authorizationStateHelper).refreshUserAuthorization(10L, 21L);
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
				mock(PermissionService.class),
				mock(AuthorizationStateHelper.class),
				mock(UserCacheAccessor.class),
				mock(UserConverter.class),
				currentUserContext,
				mock(CsrfTokenManager.class));
	}

	private UserService createService(UserMapper userMapper, UserTxService txService,
			AuthorizationStateHelper authorizationStateHelper, CurrentUserContext currentUserContext,
			UserConverter converter) {
		return new UserService(
				userMapper, mock(UserRoleMapper.class), mock(UserAssignmentMapper.class),
				mock(OrgMapper.class), mock(AttachmentService.class), txService,
				mock(PermissionService.class), authorizationStateHelper,
				mock(UserCacheAccessor.class), converter, currentUserContext,
				mock(CsrfTokenManager.class));
	}
}
