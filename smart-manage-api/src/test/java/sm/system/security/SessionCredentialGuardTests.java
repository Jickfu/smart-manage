package sm.system.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionCredentialGuardTests {
    @Test
    void missingMalformedAndOverflowingClaimsNeverReachAuthority() {
        var verifier = mock(AuthenticatedSessionStateVerifier.class);
        var session = mock(SaSession.class);
        var logic = mock(StpLogic.class);
        when(logic.getTokenSession(false)).thenReturn(session);
        try (var sessions = mockStatic(StpUtil.class)) {
            sessions.when(StpUtil::getStpLogic).thenReturn(logic);
            for (Object claim : new Object[]{null, 0L, "-1", "1.0", "01", "9999999999999999999"}) {
                when(session.get(SessionCredentialGuard.GENERATION_CLAIM)).thenReturn(claim);
                assertThrows(BizException.class, new SessionCredentialGuard(verifier)::checkCurrent);
            }
        }
        verifyNoInteractions(verifier);
    }

    @Test
    void canonicalClaimIsPassedUnchangedToPersistentVerifier() {
        var verifier = mock(AuthenticatedSessionStateVerifier.class);
        var session = mock(SaSession.class);
        var logic = mock(StpLogic.class);
        when(logic.getTokenSession(false)).thenReturn(session);
        when(session.get(SessionCredentialGuard.GENERATION_CLAIM)).thenReturn("7");
        try (var sessions = mockStatic(StpUtil.class)) {
            sessions.when(StpUtil::getStpLogic).thenReturn(logic);
            sessions.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            new SessionCredentialGuard(verifier).checkCurrent();
        }
        verify(verifier).verify(1L, 7L);
    }
}
