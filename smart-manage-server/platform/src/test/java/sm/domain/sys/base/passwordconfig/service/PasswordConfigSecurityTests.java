package sm.domain.sys.base.passwordconfig.service;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import sm.domain.sys.base.passwordconfig.mapper.PasswordConfigMapper;
import sm.domain.sys.base.passwordconfig.model.form.PasswordConfigSaveForm;
import sm.domain.sys.base.passwordconfig.converter.PasswordConfigConverter;
import sm.system.security.authorization.AdministratorOnlyAspect;
import sm.system.security.context.CurrentUserContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordConfigSecurityTests {
    @Test
    void nonAdministratorCannotReadManagementDetailOrSaveEvenWithControllerPermission() {
        var mapper = mock(PasswordConfigMapper.class);
        var transaction = mock(PasswordConfigTxService.class);
        var identity = mock(CurrentUserContext.class);
        doThrow(new IllegalStateException("需要管理员身份")).when(identity).checkAdministrator();
        var factory = new AspectJProxyFactory(new PasswordConfigService(mapper, transaction, mock(PasswordConfigConverter.class)));
        factory.addAspect(new AdministratorOnlyAspect(identity));
        PasswordConfigService service = factory.getProxy();
        assertThrows(IllegalStateException.class, service::singleton);
        assertThrows(IllegalStateException.class, () -> service.save(new PasswordConfigSaveForm()));
        verifyNoInteractions(mapper, transaction);
    }

    @Test
    void missingOrUnavailableConfigurationCannotFallBackToDefaults() {
        var mapper = mock(PasswordConfigMapper.class);
        var service = new PasswordConfigService(mapper, mock(PasswordConfigTxService.class), mock(PasswordConfigConverter.class));
        assertThrows(sm.system.exception.BizException.class, service::policy);
        when(mapper.current()).thenThrow(new IllegalStateException("数据库不可用"));
        assertThrows(IllegalStateException.class, service::policy);
    }
}
