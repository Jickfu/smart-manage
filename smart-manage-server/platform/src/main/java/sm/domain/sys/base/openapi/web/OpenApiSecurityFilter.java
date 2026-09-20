package sm.domain.sys.base.openapi.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import sm.domain.sys.base.openapi.service.OpenApiInvocationRecorder;
import sm.domain.sys.base.openapi.service.OpenApiNonceService;
import sm.domain.sys.base.openapi.service.OpenApiRuntimeAccessService;
import sm.system.exception.BizException;
import sm.system.exception.ExceptionResultResolver;
import sm.system.openapi.OpenApiActorContext;
import sm.system.openapi.OpenApiAssociatedData;
import sm.system.openapi.OpenApiEncryptedPayload;
import sm.system.openapi.OpenApiOperation;
import sm.system.openapi.OpenApiOperationRegistry;
import sm.system.openapi.OpenApiPayloadCipher;
import sm.system.openapi.OpenApiSignatureVerifier;
import sm.system.response.Result;
import sm.system.response.ResultEnum;
import sm.system.util.TraceIdUtil;
import sm.system.web.ClientIpResolver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 独立于浏览器 Cookie 认证的 OpenAPI 入站安全链路。
 * 签名始终覆盖原始请求体，验签和 nonce 消费完成后才允许解密及建立代理身份。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class OpenApiSecurityFilter extends OncePerRequestFilter {
    private static final String REQUIRED_CONTENT_TYPE = "application/json";
    private static final Pattern HEADER_TOKEN = Pattern.compile("[A-Za-z0-9._-]{8,100}");
    private static final long ALLOWED_CLOCK_SKEW_SECONDS = 300;
    private final OpenApiOperationRegistry operationRegistry;
    private final OpenApiRuntimeAccessService accessService;
    private final OpenApiNonceService nonceService;
    private final OpenApiSignatureVerifier signatureVerifier;
    private final OpenApiPayloadCipher payloadCipher;
    private final OpenApiInvocationRecorder invocationRecorder;
    private final ClientIpResolver clientIpResolver;
    private final JsonMapper jsonMapper;

    @Value("${smart-manage.system.openapi.max-envelope-bytes:1048576}")
    private int maxEnvelopeBytes;
    @Value("${smart-manage.system.openapi.max-response-bytes:2097152}")
    private int maxResponseBytes;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = requestPath(request);
        return !(path.equals("/openapi") || path.startsWith("/openapi/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        LocalDateTime requestTime = LocalDateTime.now();
        long startedNanos = System.nanoTime();
        String traceId = TraceIdUtil.generateTraceId(request);
        TraceIdUtil.setTraceId(traceId);
        String path = requestPath(request);
        String query = requestQuery(request);
        String clientIp = clientIpResolver.resolve(request);
        String keyId = request.getHeader("X-Sm-Key-Id");
        String requestId = request.getHeader("X-Sm-Request-Id");
        String operationKey = null;
        OpenApiRuntimeAccessService.AccessMaterial material = null;
        int requestBytes = 0;
        int responseBytes = 0;
        int resultCode = ResultEnum.SERVER_ERROR.getCode();
        String resultType = "AUTHENTICATION_FAILED";
        String errorMessage = null;
        try {
            OpenApiOperation operation = operationRegistry.find(request.getMethod(), path);
            if (operation == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "OpenAPI 操作不存在");
            }
            operationKey = operation.operationKey();
            validateHeaderToken(keyId);
            validateHeaderToken(requestId);
            String nonce = request.getHeader("X-Sm-Nonce");
            validateHeaderToken(nonce);
            String contentType = request.getHeader("Content-Type");
            validateContentType(contentType);
            long created = parseCreated(request.getHeader("X-Sm-Timestamp"));
            byte[] envelopeBytes = readBody(request);
            requestBytes = envelopeBytes.length;
            material = accessService.authenticate(keyId, clientIp);
            signatureVerifier.verify(envelopeBytes, request.getMethod(), path, query, contentType,
                    keyId, created, nonce,
                    request.getHeader("Content-Digest"), request.getHeader("Signature-Input"),
                    request.getHeader("Signature"), material.signingSecret());
            nonceService.consume(keyId, nonce);
            resultType = "ACCESS_DENIED";
            material = accessService.authorizeOperation(material, operation);
            byte[] plaintext = decryptRequest(material, envelopeBytes, request, keyId, created, nonce, requestId);
            resultType = "SYSTEM_FAILED";
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
            ByteArrayRequestWrapper requestWrapper = new ByteArrayRequestWrapper(request, plaintext);
            try (OpenApiActorContext.Scope ignored = OpenApiActorContext.open(material.applicationId(),
                    material.applicationNumber(), material.userId(), material.username(), material.orgId(), requestId)) {
                filterChain.doFilter(requestWrapper, responseWrapper);
            }
            byte[] plaintextResponse = responseWrapper.getContentAsByteArray();
            if (plaintextResponse.length > maxResponseBytes) {
                throw new BizException(ResultEnum.FILE_TOO_LARGE, "OpenAPI 响应超过报文大小限制");
            }
            resultCode = resultCode(plaintextResponse);
            resultType = resultCode == ResultEnum.SUCCESS.getCode() ? "SUCCESS" : "BUSINESS_FAILED";
            if ("NONE".equals(material.algorithm())) {
                responseBytes = plaintextResponse.length;
                responseWrapper.setHeader("Cache-Control", "no-store");
                responseWrapper.setHeader("X-Sm-Request-Id", requestId);
                responseWrapper.copyBodyToResponse();
            } else {
                OpenApiAssociatedData responseAad = new OpenApiAssociatedData("1", material.algorithm(), keyId,
                        "response", request.getMethod(), path, query, created, nonce, requestId);
                OpenApiEncryptedPayload encryptedResponse = payloadCipher.encrypt(plaintextResponse,
                        material.algorithm(), keyId, material.responseEncryptionKey(), responseAad);
                byte[] outerResponse = jsonMapper.writeValueAsBytes(Result.success(encryptedResponse));
                responseBytes = outerResponse.length;
                response.resetBuffer();
                response.setStatus(HttpServletResponse.SC_OK);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType("application/json;charset=UTF-8");
                response.setHeader("Cache-Control", "no-store");
                response.setHeader("X-Sm-Request-Id", requestId);
                response.setContentLength(outerResponse.length);
                response.getOutputStream().write(outerResponse);
            }
        } catch (Throwable exception) {
            Result<String> error = publicError(exception);
            resultCode = error.getCode();
            if (resultType.equals("SYSTEM_FAILED") && resultCode != ResultEnum.SERVER_ERROR.getCode()) {
                resultType = resultCode == ResultEnum.PERMISSION_ERROR.getCode()
                        ? "ACCESS_DENIED" : "BUSINESS_FAILED";
            }
            errorMessage = error.getMsg();
            byte[] errorBytes = jsonMapper.writeValueAsBytes(error);
            responseBytes = errorBytes.length;
            response.resetBuffer();
            response.setStatus(httpStatus(resultCode));
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.setContentLength(errorBytes.length);
            response.getOutputStream().write(errorBytes);
        } finally {
            long durationMs = Math.max(0, (System.nanoTime() - startedNanos) / 1_000_000);
            invocationRecorder.record(new OpenApiInvocationRecorder.RecordCommand(requestTime,
                    material == null ? null : material.applicationId(),
                    material == null ? null : material.applicationNumber(), keyId, operationKey,
                    requestId, traceId, clientIp, resultType, resultCode,
                    durationMs, requestBytes, responseBytes, errorMessage));
            TraceIdUtil.clear();
        }
    }

    private Result<String> publicError(Throwable exception) {
        Result<String> resolved = ExceptionResultResolver.resolve(exception);
        if (resolved.getCode() == ResultEnum.UNAUTHORIZED.getCode()) {
            return Result.error(ResultEnum.UNAUTHORIZED, "OpenAPI 请求认证失败");
        }
        return resolved;
    }

    private byte[] decryptRequest(OpenApiRuntimeAccessService.AccessMaterial material, byte[] requestBody,
                                  HttpServletRequest request, String keyId, long created,
                                  String nonce, String requestId) throws IOException {
        if ("NONE".equals(material.algorithm())) {
            return requestBody;
        }
        OpenApiEncryptedPayload envelope = jsonMapper.readValue(requestBody, OpenApiEncryptedPayload.class);
        if (!material.algorithm().equals(envelope.algorithm()) || !keyId.equals(envelope.keyId())) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "OpenAPI 请求认证失败");
        }
        OpenApiAssociatedData requestAad = new OpenApiAssociatedData("1", material.algorithm(), keyId,
                "request", request.getMethod(), requestPath(request), requestQuery(request), created, nonce, requestId);
        return payloadCipher.decrypt(envelope, material.requestEncryptionKey(), requestAad);
    }

    private int resultCode(byte[] responseBody) {
        try {
            JsonNode root = jsonMapper.readTree(responseBody);
            JsonNode code = root.get("code");
            return code == null || !code.isNumber() ? ResultEnum.SERVER_ERROR.getCode() : code.asInt();
        } catch (RuntimeException exception) {
            return ResultEnum.SERVER_ERROR.getCode();
        }
    }

    private byte[] readBody(HttpServletRequest request) throws IOException {
        byte[] bytes = request.getInputStream().readNBytes(maxEnvelopeBytes + 1);
        if (bytes.length == 0) {
            throw new BizException(ResultEnum.BAD_REQUEST, "OpenAPI 请求体不能为空");
        }
        if (bytes.length > maxEnvelopeBytes) {
            throw new BizException(ResultEnum.FILE_TOO_LARGE, "OpenAPI 请求体超过大小限制");
        }
        return bytes;
    }

    private long parseCreated(String value) {
        try {
            long created = Long.parseLong(value);
            long now = Instant.now().getEpochSecond();
            if (!isWithinClockSkew(created, now)) {
                throw new NumberFormatException();
            }
            return created;
        } catch (RuntimeException exception) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "OpenAPI 请求认证失败");
        }
    }

    static boolean isWithinClockSkew(long created, long now) {
        // 当前 epoch seconds 远离 long 边界，先计算允许区间可避免 now - created 的减法溢出。
        long earliest = now - ALLOWED_CLOCK_SKEW_SECONDS;
        long latest = now + ALLOWED_CLOCK_SKEW_SECONDS;
        return created >= earliest && created <= latest;
    }

    private void validateHeaderToken(String value) {
        if (value == null || !HEADER_TOKEN.matcher(value).matches()) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "OpenAPI 请求认证失败");
        }
    }

    void validateContentType(String contentType) {
        if (!REQUIRED_CONTENT_TYPE.equals(contentType)) {
            throw new BizException(ResultEnum.BAD_REQUEST, "OpenAPI Content-Type 必须为 application/json");
        }
    }

    private int httpStatus(int code) {
        return switch (code) {
            case 100401 -> HttpServletResponse.SC_UNAUTHORIZED;
            case 100403 -> HttpServletResponse.SC_FORBIDDEN;
            case 100404 -> HttpServletResponse.SC_NOT_FOUND;
            case 100413 -> HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
            default -> code >= 100500 ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                    : HttpServletResponse.SC_BAD_REQUEST;
        };
    }

    private String requestPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        return requestUri.substring(Math.min(contextPath.length(), requestUri.length()));
    }

    String requestQuery(HttpServletRequest request) {
        String rawQuery = request.getQueryString();
        // RFC 9421 规定 @query 始终包含前导问号；查询串缺失时组件值为单个问号。
        return rawQuery == null ? "?" : "?" + rawQuery;
    }

    private static final class ByteArrayRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        private ByteArrayRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return input.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    // 当前请求体已完整位于内存，不需要异步读取回调。
                }

                @Override
                public int read() {
                    return input.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }

        @Override
        public String getContentType() {
            return "application/json";
        }
    }
}
