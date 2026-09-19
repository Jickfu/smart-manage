package sm.domain.sys.base.weakpassword.service;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import sm.domain.sys.base.weakpassword.converter.WeakPasswordConverter;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordDeleteForm;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordListForm;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordSaveForm;
import sm.system.aop.log.BizLogAspect;
import sm.system.aop.log.OperateLogWriter;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.authorization.AdministratorOnlyAspect;
import sm.system.security.context.CurrentOperatorProvider;
import sm.system.security.context.CurrentUserContext;
import sm.system.web.ClientIpResolver;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeakPasswordSecurityTests {
    @Test
    void everyMaintenanceEntryRejectsNonAdministratorBeforeQueryTransactionOrAudit() {
        var mapper = mock(WeakPasswordMapper.class);
        var converter = mock(WeakPasswordConverter.class);
        var transaction = mock(WeakPasswordTxService.class);
        var audit = mock(OperateLogWriter.class);
        var operator = mock(CurrentOperatorProvider.class);
        var identity = mock(CurrentUserContext.class);
        doThrow(new BizException(ResultEnum.PERMISSION_ERROR, "禁止")).when(identity).checkAdministrator();
        var proxy = new AspectJProxyFactory(new WeakPasswordService(mapper, converter, transaction));
        proxy.addAspect(new AdministratorOnlyAspect(identity));
        proxy.addAspect(new BizLogAspect(JsonMapper.builder().build(), audit, operator, mock(ClientIpResolver.class)));
        WeakPasswordService service = proxy.getProxy();
        assertThrows(BizException.class, () -> service.listPage(new WeakPasswordListForm()));
        assertThrows(BizException.class, () -> service.detail(1L));
        assertThrows(BizException.class, () -> service.save(new WeakPasswordSaveForm()));
        assertThrows(BizException.class, () -> service.delete(new WeakPasswordDeleteForm()));
        verifyNoInteractions(mapper, converter, transaction, audit, operator);
    }
}
