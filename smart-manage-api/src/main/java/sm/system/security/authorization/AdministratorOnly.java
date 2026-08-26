package sm.system.security.authorization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记必须由真实 administrator 身份访问的公开业务入口。
 *
 * <p>类级标记保护该 Spring Bean 的全部公开方法；混合职责 Service 使用方法级标记。</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdministratorOnly {
}
