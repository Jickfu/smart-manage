package sm.domain.sys.base.passwordconfig.service;

import sm.domain.sys.base.passwordconfig.model.vo.PasswordPolicyVO;
import static org.mockito.Mockito.*;

public final class PasswordPolicyFixtures {
    private PasswordPolicyFixtures() {}
    public static PasswordConfigService defaults() {
        var config = mock(PasswordConfigService.class);
        when(config.policy()).thenReturn(new PasswordPolicyVO(15, 64, false));
        return config;
    }
}
