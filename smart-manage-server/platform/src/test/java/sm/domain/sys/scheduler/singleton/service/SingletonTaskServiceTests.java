package sm.domain.sys.scheduler.singleton.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import sm.domain.sys.scheduler.singleton.contract.*;
import sm.domain.sys.scheduler.singleton.converter.SingletonTaskConverter;
import sm.domain.sys.scheduler.singleton.model.form.SingletonCommandForm;
import sm.domain.sys.scheduler.singleton.model.form.SingletonListForm;
import sm.system.security.authorization.AdministratorOnlyAspect;
import sm.system.security.context.CurrentUserContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SingletonTaskServiceTests {
    @Test void rejectsNonAdministratorBeforeReadingOrControllingTasks() {
        var registry = mock(SingletonTaskRegistry.class);
        var accessor = mock(SingletonTaskAccessor.class);
        var transactions = mock(SingletonTaskTxService.class);
        var converter = mock(SingletonTaskConverter.class);
        var currentUser = mock(CurrentUserContext.class);
        doThrow(new IllegalStateException("拒绝普通账号")).when(currentUser).checkAdministrator();
        var factory = new AspectJProxyFactory(new SingletonTaskService(registry, accessor, transactions, converter));
        factory.setProxyTargetClass(true);
        factory.addAspect(new AdministratorOnlyAspect(currentUser));
        SingletonTaskService service = factory.getProxy();
        assertThrows(IllegalStateException.class, () -> service.listPage(new SingletonListForm()));
        assertThrows(IllegalStateException.class, () -> service.detail("test:loop"));
        assertThrows(IllegalStateException.class, () -> service.events("test:loop"));
        assertThrows(IllegalStateException.class, () -> service.control(new SingletonCommandForm()));
        verifyNoInteractions(registry, accessor, transactions, converter);
    }
    @Test void duplicateTaskIdentityFailsAndBusinessValidationPrecedesControlWrite() {
        var task = mock(SingletonTask.class);
        when(task.definition()).thenReturn(new SingletonTaskDefinition("test:loop", "循环测试", "scheduler", "测试", true, false, 60));
        assertThrows(IllegalStateException.class, () -> new SingletonTaskRegistry(List.of(task, task)));
        var transactions = mock(SingletonTaskTxService.class);
        var service = new SingletonTaskService(new SingletonTaskRegistry(List.of(task)), mock(SingletonTaskAccessor.class), transactions, mock(SingletonTaskConverter.class));
        var command = new SingletonCommandForm();
        command.setNumber("test:loop"); command.setVersion(0); command.setRequested(true);
        doThrow(new IllegalStateException("业务配置未就绪")).when(task).validateControl(true);
        assertThrows(IllegalStateException.class, () -> service.control(command));
        verifyNoInteractions(transactions);
    }
}
