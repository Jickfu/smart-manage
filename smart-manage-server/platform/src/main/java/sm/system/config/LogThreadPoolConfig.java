package sm.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 日志线程池配置
 *
 * @author Chekfu
 */
@Component
@Data
@ConfigurationProperties(prefix = "smart-manage.system.log")
public class LogThreadPoolConfig {
	private final TraceIdTaskDecorator traceIdTaskDecorator;

	private int corePoolSize;
	private int maxPoolSize;
	private int queueCapacity;
	private int keepAliveSeconds;
	private String threadNamePrefix;

	@Bean
	public ThreadPoolTaskExecutor logTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		// 设置核心线程数
		taskExecutor.setCorePoolSize(corePoolSize);
		// 设置最大线程数
		taskExecutor.setMaxPoolSize(maxPoolSize);
		// 设置队列容量
		taskExecutor.setQueueCapacity(queueCapacity);
		// 设置线程活跃时间（秒）
		taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
		// 设置默认线程名称
		taskExecutor.setThreadNamePrefix(threadNamePrefix);
		// 显式传播 Trace ID，并在线程复用前恢复上下文。
		taskExecutor.setTaskDecorator(traceIdTaskDecorator);
		// 设置拒绝策略
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		// 等待所有任务结束后再关闭线程池
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		// 线程初始化（必须在所有参数 set 完成后）
		taskExecutor.initialize();
		return taskExecutor;
	}
}
