package sm.system.config;

import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;
import sm.system.util.TraceIdUtil;

/**
 * 将任务提交线程的 Trace ID 显式传播到复用线程，并在任务结束后恢复线程原上下文。
 */
@Component
public class TraceIdTaskDecorator implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable runnable) {
		return TraceIdUtil.wrap(runnable);
	}
}
