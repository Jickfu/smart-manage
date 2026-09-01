package sm.domain.sys.base.user.service;

import sm.domain.sys.base.user.converter.UserConverter;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.service.OrgReferenceService;
import sm.domain.sys.base.org.model.OrgType;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.system.auth.SessionTerminationReason;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;
import sm.system.security.crypto.BrowserPasswordCipher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileServiceTests {
    @Test
    void missingCurrentUserTerminatesSessionAndReturnsUnauthorized() {
        UserMapper mapper = mock(UserMapper.class);
        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(10L);
        AuthorizationStateHelper stateHelper = mock(AuthorizationStateHelper.class);
        UserProfileService service = service(mapper, mock(UserAssignmentMapper.class),
                mock(OrgMapper.class), context, stateHelper);

        BizException exception = assertThrows(BizException.class, service::current);

        assertEquals(ResultEnum.UNAUTHORIZED.getCode(), exception.getCode());
        verify(stateHelper).terminateUsers(List.of(10L), SessionTerminationReason.ACCOUNT_DELETED);
    }

    @Test
    void disabledCurrentUserTerminatesSessionAndReturnsUnauthorized() {
        UserMapper mapper = mock(UserMapper.class);
        UserEntity user = new UserEntity();
        user.setEnabled(false);
        when(mapper.selectById(10L)).thenReturn(user);
        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(10L);
        AuthorizationStateHelper stateHelper = mock(AuthorizationStateHelper.class);
        UserProfileService service = service(mapper, mock(UserAssignmentMapper.class),
                mock(OrgMapper.class), context, stateHelper);

        assertThrows(BizException.class, service::current);

        verify(stateHelper).terminateUsers(List.of(10L), SessionTerminationReason.ACCOUNT_DISABLED);
    }

    @Test
    void switchCurrentOrganizationOnlyAcceptsAvailableAssignment() {
        UserAssignmentMapper assignmentMapper = mock(UserAssignmentMapper.class);
        when(assignmentMapper.selectOne(any())).thenReturn(new UserAssignmentEntity());
        OrgMapper orgMapper = mock(OrgMapper.class);
        OrgEntity organization = new OrgEntity();
        organization.setId(20L);
        organization.setOrgType(OrgType.DEPARTMENT);
        organization.setEnabled(true);
        organization.setArchived(false);
        when(orgMapper.selectById(20L)).thenReturn(organization);
        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(10L);
        UserProfileService service = service(mock(UserMapper.class), assignmentMapper, orgMapper,
                context, mock(AuthorizationStateHelper.class));

        service.switchCurrentOrganization(20L);

        verify(context).setOrgId(20L);
    }

    @Test
    void switchCurrentOrganizationRejectsUnknownAssignment() {
        UserAssignmentMapper assignmentMapper = mock(UserAssignmentMapper.class);
        when(assignmentMapper.selectOne(any())).thenReturn(null);
        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(10L);
        UserProfileService service = service(mock(UserMapper.class), assignmentMapper,
                mock(OrgMapper.class), context, mock(AuthorizationStateHelper.class));

        assertThrows(BizException.class, () -> service.switchCurrentOrganization(20L));

        verify(context, never()).setOrgId(any());
    }

    @Test
    void currentUserContainsAbsoluteRootOrganizationName() {
        UserMapper userMapper = mock(UserMapper.class);
        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setEnabled(true);
        when(userMapper.selectById(10L)).thenReturn(user);

        UserAssignmentMapper assignmentMapper = mock(UserAssignmentMapper.class);
        UserAssignmentEntity assignment = new UserAssignmentEntity();
        assignment.setId(100L);
        assignment.setOrgId(30L);
        assignment.setIsPrimary(true);
        when(assignmentMapper.selectList(any())).thenReturn(List.of(assignment));

        OrgEntity department = organization(30L, 20L, "研发部", OrgType.DEPARTMENT);
        OrgEntity company = organization(20L, 1L, "华东公司", OrgType.COMPANY);
        OrgEntity root = organization(1L, null, "集团总部", OrgType.GROUP);
        OrgMapper orgMapper = mock(OrgMapper.class);
        when(orgMapper.selectByIds(any())).thenReturn(List.of(department));
        when(orgMapper.selectById(20L)).thenReturn(company);
        when(orgMapper.selectById(1L)).thenReturn(root);

        CurrentUserContext context = mock(CurrentUserContext.class);
        when(context.getUserId()).thenReturn(10L);
        when(context.getOrgId()).thenReturn(30L);
        UserConverter converter = mock(UserConverter.class);
        when(converter.toInfoVO(user)).thenReturn(new UserInfoVO());
        UserProfileService service = new UserProfileService(userMapper, assignmentMapper,
                new OrgReferenceService(orgMapper),
                mock(AttachmentService.class), mock(UserTxService.class), mock(AuthorizationStateHelper.class),
                mock(UserCacheAccessor.class), converter, context, mock(BrowserPasswordCipher.class));

        UserInfoVO result = service.current();

        assertEquals("华东公司", result.getCompanyName());
        assertEquals("集团总部", result.getRootOrgName());
    }

    private OrgEntity organization(Long id, Long parentId, String name, OrgType orgType) {
        OrgEntity organization = new OrgEntity();
        organization.setId(id);
        organization.setParentId(parentId);
        organization.setName(name);
        organization.setOrgType(orgType);
        organization.setEnabled(true);
        organization.setArchived(false);
        return organization;
    }

    private UserProfileService service(UserMapper mapper, UserAssignmentMapper assignmentMapper,
            OrgMapper orgMapper, CurrentUserContext context, AuthorizationStateHelper stateHelper) {
        return new UserProfileService(mapper, assignmentMapper, new OrgReferenceService(orgMapper),
                mock(AttachmentService.class),
                mock(UserTxService.class), stateHelper, mock(UserCacheAccessor.class),
                mock(UserConverter.class), context, mock(BrowserPasswordCipher.class));
    }
}
