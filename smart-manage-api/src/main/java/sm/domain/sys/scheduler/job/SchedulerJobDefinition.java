package sm.domain.sys.scheduler.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 可调度任务的展示元数据，用于新增任务时提供用途说明和参数模板。 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerJobDefinition {
    String description();
    String parameterTemplate() default "{}";
}
