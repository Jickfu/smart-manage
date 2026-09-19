package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.aop.log.BizLogAspect;
import sm.system.aop.log.OperateLogWriter;
import sm.system.security.authorization.AdministratorOnlyAspect;
import sm.system.security.context.CurrentUserContext;
import sm.system.security.context.CurrentOperatorProvider;
import sm.system.web.ClientIpResolver;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 使用真实路由和两个实际切面，验证拒绝发生在日志及事务命令之前。 */
class UserCredentialCommandSecurityTests {
    @Test
    void delegatedResetCannotReachAdministratorCommandOrBusinessLog() {
        var fixture = new Fixture(false);
        fixture.target.setUsername("administrator");
        assertThrows(BizException.class, () -> fixture.router.resetPassword(1L));
        verifyNoInteractions(fixture.transaction, fixture.cache, fixture.writer, fixture.operator);
    }

    @Test
    void administratorCanResetAdministratorThroughGuardedCommand() {
        var fixture = new Fixture(true);
        fixture.target.setUsername("administrator");
        when(fixture.transaction.resetAdministratorPassword(1L)).thenReturn("temporary-test-password");
        assertNotNull(fixture.router.resetPassword(1L));
        verify(fixture.identity).checkAdministrator();
        verify(fixture.transaction).resetAdministratorPassword(1L);
        verify(fixture.transaction, never()).resetPassword(any());
        verify(fixture.cache).tryRefreshUsers(java.util.List.of(1L));
    }

    @Test
    void delegatedResetOfOrdinaryUserUsesOnlyOrdinaryCommand() {
        var fixture = new Fixture(false);
        fixture.target.setUsername("ordinary-user");
        when(fixture.transaction.resetPassword(1L)).thenReturn("temporary-test-password");
        assertNotNull(fixture.router.resetPassword(1L));
        verify(fixture.transaction).resetPassword(1L);
        verify(fixture.transaction, never()).resetAdministratorPassword(any());
        verifyNoInteractions(fixture.identity);
    }

    private static class Fixture {
        final UserTxService transaction = mock(UserTxService.class);
        final UserCacheInvalidator cache = mock(UserCacheInvalidator.class);
        final OperateLogWriter writer = mock(OperateLogWriter.class);
        final CurrentOperatorProvider operator = mock(CurrentOperatorProvider.class);
        final CurrentUserContext identity = mock(CurrentUserContext.class);
        final UserEntity target = new UserEntity();
        final UserAuthenticationService router;

        Fixture(boolean administrator) {
            if (!administrator) doThrow(new BizException(ResultEnum.PERMISSION_ERROR, "禁止"))
                    .when(identity).checkAdministrator();
            var command = new AdministratorUserCredentialService(transaction, cache);
            var proxy = new AspectJProxyFactory(command);
            proxy.addAspect(new AdministratorOnlyAspect(identity));
            proxy.addAspect(new BizLogAspect(JsonMapper.builder().build(), writer, operator, mock(ClientIpResolver.class)));
            var mapper = mock(UserMapper.class);
            when(mapper.selectById(1L)).thenReturn(target);
            router = new UserAuthenticationService(mapper, mock(UserAssignmentMapper.class),
                    mock(OrgReferenceReader.class), transaction, cache,
                    new RegularUserCredentialService(transaction, cache), proxy.getProxy());
        }
    }
}
