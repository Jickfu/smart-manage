package sm.system.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import sm.system.util.TraceIdUtil;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import sm.infrastructure.http.HttpClientProperties;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 后端可信代码使用的通用同步 HTTP 调用组件。 */
@Component
public class HttpClientHelper {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json";

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final HttpClientProperties properties;

    public HttpClientHelper(HttpClient httpClient, JsonMapper jsonMapper, HttpClientProperties properties) {
        this.httpClient = httpClient;
        this.jsonMapper = jsonMapper;
        this.properties = properties;
    }

    public <T> HttpResponseData<T> execute(HttpRequestSpec spec, Class<T> responseType) {
        return executeInternal(spec, bytes -> parseBody(bytes, responseType));
    }

    public <T> HttpResponseData<T> execute(HttpRequestSpec spec,
                                           ParameterizedTypeReference<T> responseType) {
        JavaType javaType = jsonMapper.getTypeFactory().constructType(responseType.getType());
        return executeInternal(spec, bytes -> jsonMapper.readValue(bytes, javaType));
    }

    private <T> HttpResponseData<T> executeInternal(HttpRequestSpec spec, BodyReader<T> bodyReader) {
        HttpRequest request = buildRequest(spec);
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (spec.failOnNonSuccess() && !isSuccess(response.statusCode())) {
                throw new ExternalHttpException(ExternalHttpException.FailureType.HTTP_STATUS,
                        response.statusCode(), "目标服务返回 HTTP " + response.statusCode());
            }
            T body = bodyReader.read(response.body());
            return new HttpResponseData<>(response.statusCode(), response.headers().map(), body);
        } catch (ExternalHttpException exception) {
            throw exception;
        } catch (HttpTimeoutException exception) {
            throw new ExternalHttpException(ExternalHttpException.FailureType.TIMEOUT,
                    "请求目标服务超时", exception);
        } catch (ConnectException exception) {
            throw new ExternalHttpException(ExternalHttpException.FailureType.CONNECTION,
                    "无法连接目标服务", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ExternalHttpException(ExternalHttpException.FailureType.INTERRUPTED,
                    "请求目标服务被中断", exception);
        } catch (IOException exception) {
            throw new ExternalHttpException(ExternalHttpException.FailureType.CONNECTION,
                    "读取目标服务响应失败", exception);
        } catch (Exception exception) {
            throw new ExternalHttpException(ExternalHttpException.FailureType.RESPONSE_PARSE,
                    "解析目标服务响应失败", exception);
        }
    }

    private HttpRequest buildRequest(HttpRequestSpec spec) {
        try {
            URI uri = appendQueryParams(spec.uri(), spec.queryParams());
            Duration timeout = spec.timeout() == null ? properties.getRequestTimeout() : spec.timeout();
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(timeout);
            spec.headers().forEach((name, values) -> values.forEach(value -> builder.header(name, value)));
            addTraceId(builder, spec.headers());
            builder.method(spec.method(), createBodyPublisher(builder, spec));
            return builder.build();
        } catch (ExternalHttpException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ExternalHttpException(ExternalHttpException.FailureType.REQUEST_BUILD,
                    "构造 HTTP 请求失败", exception);
        }
    }

    private HttpRequest.BodyPublisher createBodyPublisher(HttpRequest.Builder builder, HttpRequestSpec spec)
            throws IOException {
        Object body = spec.body();
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        if (body instanceof byte[] bytes) {
            return HttpRequest.BodyPublishers.ofByteArray(bytes);
        }
        if (body instanceof String text) {
            return HttpRequest.BodyPublishers.ofString(text, StandardCharsets.UTF_8);
        }
        if (!containsHeader(spec.headers(), CONTENT_TYPE)) {
            builder.header(CONTENT_TYPE, JSON_CONTENT_TYPE);
        }
        return HttpRequest.BodyPublishers.ofByteArray(jsonMapper.writeValueAsBytes(body));
    }

    @SuppressWarnings("unchecked")
    private <T> T parseBody(byte[] body, Class<T> responseType) throws IOException {
        if (responseType == Void.class) {
            return null;
        }
        if (responseType == byte[].class) {
            return (T) body;
        }
        if (responseType == String.class) {
            return (T) new String(body, StandardCharsets.UTF_8);
        }
        return jsonMapper.readValue(body, responseType);
    }

    private URI appendQueryParams(URI uri, Map<String, List<String>> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        queryParams.forEach((name, values) -> values.forEach(value -> builder.queryParam(name, value)));
        return builder.build().encode(StandardCharsets.UTF_8).toUri();
    }

    private void addTraceId(HttpRequest.Builder builder, Map<String, List<String>> headers) {
        String traceId = TraceIdUtil.getTraceId();
        if (traceId != null && !traceId.isBlank() && !containsHeader(headers, TraceIdUtil.TRACE_ID_STRING)) {
            builder.header(TraceIdUtil.TRACE_ID_STRING, traceId);
        }
    }

    private boolean containsHeader(Map<String, List<String>> headers, String expectedName) {
        String normalizedName = expectedName.toLowerCase(Locale.ROOT);
        return headers.keySet().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedName::equals);
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    @FunctionalInterface
    private interface BodyReader<T> {
        T read(byte[] body) throws Exception;
    }
}
