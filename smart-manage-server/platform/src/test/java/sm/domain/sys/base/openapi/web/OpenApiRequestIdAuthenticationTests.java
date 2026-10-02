package sm.domain.sys.base.openapi.web;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import sm.domain.sys.base.openapi.service.OpenApiInvocationRecorder;
import sm.domain.sys.base.openapi.service.OpenApiNonceService;
import sm.domain.sys.base.openapi.service.OpenApiRuntimeAccessService;
import sm.system.openapi.OpenApiActorContext;
import sm.system.openapi.OpenApiAssociatedData;
import sm.system.openapi.OpenApiEncryptedPayload;
import sm.system.openapi.OpenApiOperation;
import sm.system.openapi.OpenApiOperationRegistry;
import sm.system.openapi.OpenApiPayloadCipher;
import sm.system.openapi.OpenApiSignatureVerifier;
import sm.system.response.ResultEnum;
import sm.system.web.ClientIpResolver;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 用真实 HMAC、GCM 和过滤器验证三种模式在建立代理身份前均认证 requestId。 */
class OpenApiRequestIdAuthenticationTests {
    private static final String PATH = "/openapi/test";
    private static final String KEY_ID = "sm_test_key";
    private static final String NONCE = "nonce-123456";
    private static final String REQUEST_ID = "request-123456";
    private static final byte[] SIGNING_SECRET = "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PLAINTEXT = "{\"categoryNumber\":\"test\"}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] RESPONSE = "{\"code\":0,\"data\":{}}".getBytes(StandardCharsets.UTF_8);

    @ParameterizedTest
    @ValueSource(strings = {"NONE", "AES-256-GCM", "SM4-GCM"})
    void signedRequestIdReachesActorResponseAndSuccessAudit(String algorithm) throws Exception {
        var fixture = new Fixture(algorithm);
        var response = new MockHttpServletResponse();
        fixture.filter.doFilter(fixture.request(), response, (request, servletResponse) -> {
            assertEquals(REQUEST_ID, OpenApiActorContext.requireCurrent().requestId());
            assertArrayEquals(PLAINTEXT, request.getInputStream().readAllBytes());
            servletResponse.getOutputStream().write(RESPONSE);
        });

        assertEquals(200, response.getStatus());
        assertEquals(REQUEST_ID, response.getHeader("X-Sm-Request-Id"));
        if ("NONE".equals(algorithm)) {
            assertArrayEquals(RESPONSE, response.getContentAsByteArray());
        } else {
            var envelope = fixture.mapper.treeToValue(fixture.mapper.readTree(response.getContentAsByteArray()).get("data"),
                    OpenApiEncryptedPayload.class);
            assertArrayEquals(RESPONSE, fixture.cipher.decrypt(envelope, fixture.encryptionKey,
                    new OpenApiAssociatedData("1", algorithm, KEY_ID, "response", "POST", PATH,
                            "?", fixture.created, NONCE, REQUEST_ID)));
        }
        verify(fixture.nonceService).consume(KEY_ID, NONCE);
        var audit = fixture.audit();
        assertEquals("SUCCESS", audit.resultType());
        assertEquals(REQUEST_ID, audit.requestId());
        assertNull(OpenApiActorContext.currentOrNull());
    }

    @ParameterizedTest
    @ValueSource(strings = {"NONE", "AES-256-GCM", "SM4-GCM"})
    void replacingOnlyRequestIdRejectsBeforeNonceAuthorizationOrBusiness(String algorithm) throws Exception {
        var fixture = new Fixture(algorithm);
        var request = fixture.request();
        request.removeHeader("X-Sm-Request-Id");
        request.addHeader("X-Sm-Request-Id", "request-attacker");
        var response = new MockHttpServletResponse();
        var businessExecuted = new AtomicBoolean();
        fixture.filter.doFilter(request, response, (servletRequest, servletResponse) -> businessExecuted.set(true));

        assertEquals(401, response.getStatus());
        assertEquals(ResultEnum.UNAUTHORIZED.getCode(), fixture.mapper.readTree(response.getContentAsByteArray()).get("code").asInt());
        assertFalse(businessExecuted.get());
        assertNull(OpenApiActorContext.currentOrNull());
        assertNull(response.getHeader("X-Sm-Request-Id"));
        verifyNoInteractions(fixture.nonceService);
        verify(fixture.accessService, never()).authorizeOperation(any(), any());
        assertEquals("AUTHENTICATION_FAILED", fixture.audit().resultType());
    }

    private static class Fixture {
        final JsonMapper mapper = JsonMapper.builder().build();
        final OpenApiPayloadCipher cipher = new OpenApiPayloadCipher();
        final OpenApiRuntimeAccessService accessService = mock(OpenApiRuntimeAccessService.class);
        final OpenApiNonceService nonceService = mock(OpenApiNonceService.class);
        final OpenApiInvocationRecorder recorder = mock(OpenApiInvocationRecorder.class);
        final long created = Instant.now().getEpochSecond();
        final byte[] encryptionKey;
        final byte[] body;
        final OpenApiSecurityFilter filter;

        Fixture(String algorithm) throws Exception {
            encryptionKey = "SM4-GCM".equals(algorithm) ? new byte[16] : new byte[32];
            body = "NONE".equals(algorithm) ? PLAINTEXT : mapper.writeValueAsBytes(cipher.encrypt(PLAINTEXT,
                    algorithm, KEY_ID, encryptionKey, new OpenApiAssociatedData("1", algorithm, KEY_ID,
                            "request", "POST", PATH, "?", created, NONCE, REQUEST_ID)));
            var material = new OpenApiRuntimeAccessService.AccessMaterial(10L, "test-app", 20L,
                    "ordinary-user", 30L, algorithm, KEY_ID, SIGNING_SECRET, encryptionKey, encryptionKey);
            when(accessService.authenticate(eq(KEY_ID), any())).thenReturn(material);
            when(accessService.authorizeOperation(eq(material), any())).thenReturn(material);
            var registry = mock(OpenApiOperationRegistry.class);
            when(registry.find("POST", PATH)).thenReturn(new OpenApiOperation("test.operation", "test.api", "v1",
                    "测试操作", "POST", PATH, "sys", "系统", "base", "基础", "test", "测试"));
            filter = new OpenApiSecurityFilter(registry, accessService, nonceService,
                    new OpenApiSignatureVerifier(), cipher, recorder, mock(ClientIpResolver.class), mapper);
            ReflectionTestUtils.setField(filter, "maxEnvelopeBytes", 1048576);
            ReflectionTestUtils.setField(filter, "maxResponseBytes", 2097152);
        }

        MockHttpServletRequest request() throws Exception {
            var request = new MockHttpServletRequest("POST", PATH);
            request.setContent(body);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("X-Sm-Key-Id", KEY_ID);
            request.addHeader("X-Sm-Timestamp", Long.toString(created));
            request.addHeader("X-Sm-Nonce", NONCE);
            request.addHeader("X-Sm-Request-Id", REQUEST_ID);
            String digest = "sha-256=:" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(body)) + ":";
            String input = "sm1=(\"@method\" \"@path\" \"@query\" \"content-type\" \"content-digest\" \"x-sm-key-id\" "
                    + "\"x-sm-timestamp\" \"x-sm-nonce\" \"x-sm-request-id\");created=" + created
                    + ";keyid=\"" + KEY_ID + "\";nonce=\"" + NONCE + "\";alg=\"hmac-sha256\"";
            // 独立客户端基串，不调用服务端的组件列表或签名构造器。
            String base = "\"@method\": POST\n\"@path\": " + PATH + "\n\"@query\": ?\n\"content-type\": application/json"
                    + "\n\"content-digest\": " + digest + "\n\"x-sm-key-id\": " + KEY_ID
                    + "\n\"x-sm-timestamp\": " + created + "\n\"x-sm-nonce\": " + NONCE
                    + "\n\"x-sm-request-id\": " + REQUEST_ID + "\n\"@signature-params\": " + input.substring(4);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SIGNING_SECRET, "HmacSHA256"));
            request.addHeader("Content-Digest", digest);
            request.addHeader("Signature-Input", input);
            request.addHeader("Signature", "sm1=:" + Base64.getEncoder().encodeToString(mac.doFinal(base.getBytes(StandardCharsets.UTF_8))) + ":");
            return request;
        }

        OpenApiInvocationRecorder.RecordCommand audit() {
            var capture = ArgumentCaptor.forClass(OpenApiInvocationRecorder.RecordCommand.class);
            verify(recorder).record(capture.capture());
            return capture.getValue();
        }
    }
}
