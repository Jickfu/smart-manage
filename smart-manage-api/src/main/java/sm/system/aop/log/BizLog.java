package sm.system.aop.log;

import java.lang.annotation.*;

/**
 * 业务日志注解
 *
 * @author Chekfu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BizLog {
	/**
	 * 日志名称，如「创建用户」
	 */
	String value() default "未命名";

	/**
	 * 是否落库方法参数（会脱敏/截断）
	 */
	boolean recordRequest() default true;

	/**
	 * 是否落库返回体（会脱敏/截断）
	 */
	boolean recordResponse() default true;

	int maxParamLen() default 4000;

	int maxResponseLen() default 4000;
}
