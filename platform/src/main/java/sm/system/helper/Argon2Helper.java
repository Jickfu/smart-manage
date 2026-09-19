package sm.system.helper;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Argon2 密码工具，参数可通过配置文件覆盖。
 *
 * @author Chekfu
 */
@Component
public class Argon2Helper {

	private static final Argon2 ARGON2 = Argon2Factory.create();

	private static int iterations = 2;
	private static int memory = 65536;
	private static int parallelism = 1;

	@Value("${smart-manage.system.security.argon2.iterations:2}")
	public void setIterations(int val) {
		Argon2Helper.iterations = val;
	}

	@Value("${smart-manage.system.security.argon2.memory:65536}")
	public void setMemory(int val) {
		Argon2Helper.memory = val;
	}

	@Value("${smart-manage.system.security.argon2.parallelism:1}")
	public void setParallelism(int val) {
		Argon2Helper.parallelism = val;
	}

	/**
	 * 加密密码
	 */
	public static String encode(String password) {
		return ARGON2.hash(iterations, memory, parallelism, password.toCharArray());
	}

	/**
	 * 验证密码
	 */
	public static boolean verify(String hash, String password) {
		return ARGON2.verify(hash, password.toCharArray());
	}
}
