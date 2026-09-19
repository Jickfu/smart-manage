package sm.system.http;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 一次后端出站 HTTP 请求的描述。
 *
 * <p>仅供可信后端代码构造，不应直接绑定前端提交的 URL、请求头或请求体。</p>
 */
public final class HttpRequestSpec {
    private final String method;
    private final URI uri;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> queryParams;
    private final Object body;
    private final Duration timeout;
    private final boolean failOnNonSuccess;

    private HttpRequestSpec(Builder builder) {
        method = builder.method;
        uri = builder.uri;
        headers = immutableCopy(builder.headers);
        queryParams = immutableCopy(builder.queryParams);
        body = builder.body;
        timeout = builder.timeout;
        failOnNonSuccess = builder.failOnNonSuccess;
    }

    public static Builder builder(String method, URI uri) {
        return new Builder(method, uri);
    }

    public static Builder get(URI uri) {
        return builder("GET", uri);
    }

    public static Builder post(URI uri) {
        return builder("POST", uri);
    }

    public static Builder put(URI uri) {
        return builder("PUT", uri);
    }

    public static Builder patch(URI uri) {
        return builder("PATCH", uri);
    }

    public static Builder delete(URI uri) {
        return builder("DELETE", uri);
    }

    public String method() {
        return method;
    }

    public URI uri() {
        return uri;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public Map<String, List<String>> queryParams() {
        return queryParams;
    }

    public Object body() {
        return body;
    }

    public Duration timeout() {
        return timeout;
    }

    public boolean failOnNonSuccess() {
        return failOnNonSuccess;
    }

    private static Map<String, List<String>> immutableCopy(Map<String, List<String>> source) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        source.forEach((name, values) -> result.put(name, List.copyOf(values)));
        return Collections.unmodifiableMap(result);
    }

    public static final class Builder {
        private final String method;
        private final URI uri;
        private final Map<String, List<String>> headers = new LinkedHashMap<>();
        private final Map<String, List<String>> queryParams = new LinkedHashMap<>();
        private Object body;
        private Duration timeout;
        private boolean failOnNonSuccess = true;

        private Builder(String method, URI uri) {
            this.method = normalizeMethod(method);
            this.uri = validateUri(uri);
        }

        public Builder header(String name, String value) {
            addValue(headers, name, value);
            return this;
        }

        public Builder queryParam(String name, Object value) {
            addValue(queryParams, name, Objects.toString(value, ""));
            return this;
        }

        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        public Builder timeout(Duration timeout) {
            if (timeout == null || timeout.isZero() || timeout.isNegative()) {
                throw new IllegalArgumentException("请求超时必须大于 0");
            }
            this.timeout = timeout;
            return this;
        }

        public Builder failOnNonSuccess(boolean failOnNonSuccess) {
            this.failOnNonSuccess = failOnNonSuccess;
            return this;
        }

        public HttpRequestSpec build() {
            if ((method.equals("GET") || method.equals("HEAD")) && body != null) {
                throw new IllegalArgumentException(method + " 请求不能携带请求体");
            }
            return new HttpRequestSpec(this);
        }

        private static void addValue(Map<String, List<String>> values, String name, String value) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("请求参数名称不能为空");
            }
            values.computeIfAbsent(name, ignored -> new ArrayList<>()).add(Objects.requireNonNull(value));
        }

        private static String normalizeMethod(String method) {
            if (method == null || method.isBlank()) {
                throw new IllegalArgumentException("HTTP 方法不能为空");
            }
            String normalized = method.trim().toUpperCase();
            if (!normalized.matches("[A-Z]+")) {
                throw new IllegalArgumentException("HTTP 方法不合法");
            }
            return normalized;
        }

        private static URI validateUri(URI uri) {
            Objects.requireNonNull(uri, "请求 URI 不能为空");
            if (!uri.isAbsolute() || uri.getHost() == null
                    || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException("请求 URI 必须是完整的 HTTP 或 HTTPS 地址");
            }
            return uri;
        }
    }
}
