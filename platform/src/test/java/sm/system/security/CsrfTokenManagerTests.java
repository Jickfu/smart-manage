package sm.system.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CsrfTokenManagerTests {
    private final CsrfTokenManager manager = new CsrfTokenManager();

    @Test
    void initializesIndependentThirtyTwoCharacterHexToken() {
        StpLogic stpLogic = mock(StpLogic.class);
        SaSession session = mock(SaSession.class);
        when(stpLogic.getTokenSessionByToken("login-token")).thenReturn(session);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getStpLogic).thenReturn(stpLogic);

            String csrfToken = manager.initializeSession("login-token");

            assertEquals(32, csrfToken.length());
            assertTrue(csrfToken.matches("[0-9a-f]{32}"));
            verify(session).set(eq(CsrfTokenManager.SESSION_KEY), eq(csrfToken));
        }
    }

    @Test
    void rejectsIncorrectTokenWithDedicatedErrorCode() {
        SaSession session = mock(SaSession.class);
        when(session.getString(CsrfTokenManager.SESSION_KEY))
                .thenReturn("0123456789abcdef0123456789abcdef");

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getTokenSession).thenReturn(session);

            BizException exception = assertThrows(BizException.class,
                    () -> manager.validateCurrentToken("fedcba9876543210fedcba9876543210"));

            assertEquals(ResultEnum.CSRF_TOKEN_INVALID.getCode(), exception.getCode());
        }
    }
}
