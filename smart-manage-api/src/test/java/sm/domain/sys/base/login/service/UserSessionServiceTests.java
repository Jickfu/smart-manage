package sm.domain.sys.base.login.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.system.security.CsrfTokenManager;
import sm.system.security.SessionCredentialGuard;
import sm.system.security.context.CurrentUserContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserSessionServiceTests {
    @Test
    void loginStoresAuthenticationSnapshotWithoutUpgradingIt() {
        var service = new UserSessionService(mock(CurrentUserContext.class), mock(CsrfTokenManager.class));
        var authentication = new UserAuthentication(1L, "user", "用户", false, false, 10L, 7L, null);
        var session = mock(SaSession.class);
        try (var sessions = mockStatic(StpUtil.class)) {
            sessions.when(StpUtil::getTokenSession).thenReturn(session);
            service.completeLogin(authentication);
            verify(session).set(SessionCredentialGuard.GENERATION_CLAIM, "7");
            assertThrows(IllegalStateException.class, () -> service.completeLogin(
                    new UserAuthentication(2L, "user2", "用户2", false, false, 10L, null, null)));
            sessions.verify(() -> StpUtil.login(2L), never());
        }
    }
}
