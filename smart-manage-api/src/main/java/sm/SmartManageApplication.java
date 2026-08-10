package sm;

import org.springframework.boot.SpringApplication;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Chekfu
 */
@SpringBootApplication
@EnableMethodCache(basePackages = "sm.domain")
@EnableScheduling
public class SmartManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartManageApplication.class, args);
	}

}
