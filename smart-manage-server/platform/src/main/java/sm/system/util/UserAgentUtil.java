package sm.system.util;

import cn.hutool.core.util.RandomUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @author Chekfu
 */
public class UserAgentUtil {
	//	private static final List<String> USER_AGENT_OS_LIST = Arrays.asList("(Windows NT 10.0; Win64; x64)", "(Windows NT 10.0; WOW64)",
	//			"(Windows NT 6.3; WOW64)", "(Windows NT 6.3; Win64; x64)", "(Windows NT 6.1; Win64; x64)", "(Windows NT 6.1; WOW64)", "(X11; Linux x86_64)",
	//			"(Macintosh; Intel Mac OS X 10_12_6)", "(Macintosh; Intel Mac OS X 10_15_7)");
	private static final List<String> USER_AGENT_OS_LIST = Arrays.asList("(Windows NT 10.0; Win64; x64)", "(Windows NT 10.0; WOW64)",
			"(Windows NT 6.3; WOW64)", "(Windows NT 6.3; Win64; x64)", "(Windows NT 6.1; Win64; x64)", "(Windows NT 6.1; WOW64)");
	private static final List<String> USER_AGENT_VERSION_LIST = Arrays.asList(
			"135.0.0.0", "134.0.0.0", "131.0.0.0",
			"110.0.5481.77", "110.0.5481.30", "109.0.5414.74", "108.0.5359.71", "108.0.5359.22",
			"98.0.4758.48", "97.0.4692.71");


	public static String getRandomUserAgent() {
		String randomVersion = RandomUtil.randomEle(USER_AGENT_VERSION_LIST);
		return "Mozilla/5.0 "
				// os
				+ RandomUtil.randomEle(USER_AGENT_OS_LIST)
				+ " AppleWebKit/537.36 (KHTML, like Gecko)"
				+ " Chrome/" + randomVersion
				+ " Safari/537.36";
	}

}
