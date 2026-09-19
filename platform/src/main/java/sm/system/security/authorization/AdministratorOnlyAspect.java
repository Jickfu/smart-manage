package sm.system.security.authorization;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sm.system.security.context.CurrentUserContext;

/** 统一在高风险业务入口执行真实管理员身份校验。 */
@Aspect
@Order(0)
@Component
@RequiredArgsConstructor
public class AdministratorOnlyAspect {
    private final CurrentUserContext currentUserContext;

    /**
     * 鉴权切面优先于业务日志切面执行，拒绝请求不会读取参数或触发任何业务副作用。
     */
    @Around("@within(sm.system.security.authorization.AdministratorOnly)"
            + " || @annotation(sm.system.security.authorization.AdministratorOnly)")
    public Object authorize(ProceedingJoinPoint joinPoint) throws Throwable {
        currentUserContext.checkAdministrator();
        return joinPoint.proceed();
    }
}
