package sm;

import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import sm.system.time.BusinessTime;

/**
 * @author Chekfu
 */
@SpringBootApplication
@Slf4j
@EnableMethodCache(basePackages = "sm.domain")
@EnableScheduling
public class SmartManageApplication {

	public static void main(String[] args) {
		// 启动日志、无参 now() 和 pgJDBC 会话都使用同一默认时区。
		TimeZone.setDefault(TimeZone.getTimeZone(BusinessTime.ZONE));
		SpringApplication.run(SmartManageApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logStartupSuccess() {
		// 等待应用就绪后再输出，避免初始化或启动任务失败时误报成功。
		log.info("""

				 ____  __  __    _    ____ _____   __  __    _    _   _    _    ____ _____
				/ ___||  \\/  |  / \\  |  _ \\_   _| |  \\/  |  / \\  | \\ | |  / \\  / ___| ____|
				\\___ \\| |\\/| | / _ \\ | |_) || |   | |\\/| | / _ \\ |  \\| | / _ \\| |  _|  _|
				 ___) | |  | |/ ___ \\|  _ < | |   | |  | |/ ___ \\| |\\  |/ ___ \\ |_| | |___
				|____/|_|  |_/_/   \\_\\_| \\_\\|_|   |_|  |_/_/   \\_\\_| \\_/_/   \\_\\____|_____|

				Smart Manage 启动成功
				""");
	}

}
