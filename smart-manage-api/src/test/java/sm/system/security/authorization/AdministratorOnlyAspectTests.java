package sm.system.security.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import sm.system.security.context.CurrentUserContext;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdministratorOnlyAspectTests {

    @Test
    void classLevelAnnotationMustRejectBeforeTargetExecutes() {
        CurrentUserContext currentUserContext = rejectingContext();
        ClassProtectedTarget target = new ClassProtectedTarget();
        ClassProtectedTarget proxy = proxy(target, currentUserContext);

        assertThrows(AdministratorRejectedException.class, proxy::execute);

        assertEquals(0, target.invocationCount.get());
        verify(currentUserContext).checkAdministrator();
    }

    @Test
    void methodLevelAnnotationMustOnlyProtectAnnotatedEntry() {
        CurrentUserContext currentUserContext = rejectingContext();
        MixedTarget target = new MixedTarget();
        MixedTarget proxy = proxy(target, currentUserContext);

        assertThrows(AdministratorRejectedException.class, proxy::protectedEntry);
        proxy.internalEntry();

        assertEquals(0, target.protectedInvocationCount.get());
        assertEquals(1, target.internalInvocationCount.get());
        verify(currentUserContext).checkAdministrator();
    }

    private CurrentUserContext rejectingContext() {
        CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
        doThrow(new AdministratorRejectedException())
                .when(currentUserContext).checkAdministrator();
        return currentUserContext;
    }

    @SuppressWarnings("unchecked")
    private <TYPE> TYPE proxy(TYPE target, CurrentUserContext currentUserContext) {
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAspect(new AdministratorOnlyAspect(currentUserContext));
        return (TYPE) proxyFactory.getProxy();
    }

    @AdministratorOnly
    static class ClassProtectedTarget {
        private final AtomicInteger invocationCount = new AtomicInteger();

        public void execute() {
            invocationCount.incrementAndGet();
        }
    }

    static class MixedTarget {
        private final AtomicInteger protectedInvocationCount = new AtomicInteger();
        private final AtomicInteger internalInvocationCount = new AtomicInteger();

        @AdministratorOnly
        public void protectedEntry() {
            protectedInvocationCount.incrementAndGet();
        }

        public void internalEntry() {
            internalInvocationCount.incrementAndGet();
        }
    }

    private static class AdministratorRejectedException extends RuntimeException {
    }
}
