package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserSessionStateVerifierTests {
    private final UserMapper mapper = mock(UserMapper.class);
    private final UserSessionStateVerifier verifier = new UserSessionStateVerifier(mapper);

    @Test
    void changedGenerationRejectsOldSessionButKeepsNewSession() {
        var user = new UserEntity();
        user.setEnabled(true);
        user.setCredentialGeneration(3L);
        when(mapper.selectSecurityState(1L)).thenReturn(user);
        assertEquals(ResultEnum.UNAUTHORIZED.getCode(), assertThrows(BizException.class,
                () -> verifier.verify(1L, 2L)).getCode());
        assertDoesNotThrow(() -> verifier.verify(1L, 3L));
        verify(mapper, times(2)).selectSecurityState(1L);
    }

    @Test
    void disabledDeletedAndMissingGenerationFailClosed() {
        assertThrows(BizException.class, () -> verifier.verify(1L, 0L));
        var user = new UserEntity();
        user.setEnabled(false);
        user.setCredentialGeneration(0L);
        when(mapper.selectSecurityState(1L)).thenReturn(user);
        assertThrows(BizException.class, () -> verifier.verify(1L, 0L));
        user.setEnabled(true);
        user.setCredentialGeneration(null);
        assertThrows(BizException.class, () -> verifier.verify(1L, 0L));
    }

    @Test
    void databaseOutageRemainsServerFailureNotUnauthorized() {
        var outage = new IllegalStateException("database unavailable");
        when(mapper.selectSecurityState(1L)).thenThrow(outage);
        assertSame(outage, assertThrows(IllegalStateException.class, () -> verifier.verify(1L, 0L)));
    }
}
