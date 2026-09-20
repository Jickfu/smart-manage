package sm.system.aop.log;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 操作日志正文截断与敏感字段脱敏工具。 */
public final class LogPayloadUtil {
    private static final int DEFAULT_MAX = 4000;
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldpassword", "newpassword", "captcha", "token", "authorization", "smtoken",
            "secret", "privatekey", "accesskey", "ticket", "credential");
    private static final Pattern KEY_IN_JSON = Pattern.compile(
            "\"([^\"]*(?:password|captcha|token|secret|privatekey|accesskey|ticket|credential)|authorization|smtoken)\""
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

    /**
     * 解析 JSON 树后递归清除凭据字段，覆盖嵌套对象、数组以及非字符串值。
     * 无法解析但出现敏感字段标记时整体清除，避免正则漏掉复杂值。
     */
    public static String redactJson(String json, JsonMapper jsonMapper) {
        if (json == null || json.isEmpty()) return json;
        try {
            JsonNode root = jsonMapper.readTree(json);
            redactNode(root);
            return jsonMapper.writeValueAsString(root);
        } catch (JacksonException exception) {
            return containsSensitiveMarker(json) ? "[redacted unparseable payload]" : json;
        }
    }

    private static void redactNode(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            for (String propertyName : Set.copyOf(objectNode.propertyNames())) {
                JsonNode child = objectNode.get(propertyName);
                if (isSensitiveName(propertyName)) {
                    objectNode.put(propertyName, "***");
                } else {
                    redactNode(child);
                }
            }
            return;
        }
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode child : arrayNode) redactNode(child);
        }
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
                || normalizedName.contains("credential")
                ? "***"
                : name;
    }

    public static boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEYS.contains(key.toLowerCase(Locale.ROOT));
    }

    private static boolean isSensitiveName(String name) {
        return "***".equals(maskNameLike(name));
    }

    private static boolean containsSensitiveMarker(String value) {
        String normalizedValue = value.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.stream().anyMatch(normalizedValue::contains)
                || normalizedValue.contains("password")
                || normalizedValue.contains("privatekey")
                || normalizedValue.contains("accesskey")
                || normalizedValue.contains("credential");
    }
}
