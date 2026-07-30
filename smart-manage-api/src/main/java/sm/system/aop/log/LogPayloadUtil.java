package sm.system.aop.log;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** 操作日志正文截断与敏感字段脱敏工具。 */
public final class LogPayloadUtil {
    private static final int DEFAULT_MAX = 4000;
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldpassword", "newpassword", "captcha", "token", "authorization", "smtoken",
            "secret", "privatekey", "accesskey", "ticket");
    private static final Pattern KEY_IN_JSON = Pattern.compile(
            "\"([^\"]*(?:password|captcha|token|secret|privatekey|accesskey|ticket)|authorization|smtoken)\""
                    + "\\s*:\\s*\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE);

    private LogPayloadUtil() {
    }

    public static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        int limit = max > 0 ? max : DEFAULT_MAX;
        return value.length() <= limit ? value : value.substring(0, limit) + "...(truncated)";
    }

    public static String maskJsonLike(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        return KEY_IN_JSON.matcher(json).replaceAll(matchResult ->
                "\"" + matchResult.group(1) + "\":\"***\"");
    }

    public static String maskNameLike(String name) {
        if (name == null) {
            return null;
        }
        String normalizedName = name.toLowerCase(Locale.ROOT);
        return isSensitiveKey(normalizedName)
                || normalizedName.contains("password")
                || normalizedName.contains("token")
                || normalizedName.contains("secret")
                || normalizedName.contains("privatekey")
                || normalizedName.contains("accesskey")
                || normalizedName.contains("ticket")
                ? "***"
                : name;
    }

    public static boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEYS.contains(key.toLowerCase(Locale.ROOT));
    }
}
