package sm.system.openapi;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenApiSignatureVerifierTests {
    private final OpenApiSignatureVerifier verifier = new OpenApiSignatureVerifier();

    @Test
    void strictRfc9421ProfileAcceptsValidSignatureAndRejectsTampering() throws Exception {
        byte[] body = "{\"ciphertext\":\"abc\"}".getBytes(StandardCharsets.UTF_8);
        byte[] secret = "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8);
        String keyId = "sm_test_key";
        String nonce = "nonce-123456";
        String requestId = "request-123456";
        long created = 1788163200L;
        String path = "/openapi/sys/base/basic-data/v1/items/query";
        String query = "?status=ENABLED&page=1";
        String contentType = "application/json";
        String digest = "sha-256=:" + Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(body)) + ":";
        String input = "sm1=(\"@method\" \"@path\" \"@query\" \"content-type\" \"content-digest\" \"x-sm-key-id\" "
                + "\"x-sm-timestamp\" \"x-sm-nonce\" \"x-sm-request-id\");created=" + created + ";keyid=\"" + keyId
                + "\";nonce=\"" + nonce + "\";alg=\"hmac-sha256\"";
        String base = "\"@method\": POST\n\"@path\": " + path
                + "\n\"@query\": " + query
                + "\n\"content-type\": " + contentType
                + "\n\"content-digest\": " + digest
                + "\n\"x-sm-key-id\": " + keyId
                + "\n\"x-sm-timestamp\": " + created
                + "\n\"x-sm-nonce\": " + nonce
                + "\n\"x-sm-request-id\": " + requestId
                + "\n\"@signature-params\": " + input.substring("sm1=".length());
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        String signature = "sm1=:" + Base64.getEncoder().encodeToString(
                mac.doFinal(base.getBytes(StandardCharsets.UTF_8))) + ":";

        assertDoesNotThrow(() -> verifier.verify(body, "POST", path, query, contentType, keyId, created, nonce,
                requestId, digest, input, signature, secret));
        assertThrows(BizException.class, () -> verifier.verify(
                "tampered".getBytes(StandardCharsets.UTF_8), "POST", path, query, contentType, keyId, created,
                nonce, requestId, digest, input, signature, secret));
        assertThrows(BizException.class, () -> verifier.verify(body, "POST", path,
                "?status=DISABLED&page=1", contentType, keyId, created, nonce,
                requestId, digest, input, signature, secret));
        assertThrows(BizException.class, () -> verifier.verify(body, "POST", path, query,
                "application/json;charset=UTF-8", keyId, created, nonce,
                requestId, digest, input, signature, secret));
        assertThrows(BizException.class, () -> verifier.verify(body, "POST", path, query, contentType,
                keyId, created, nonce, "request-tampered", digest, input, signature, secret));

        // 即使旧签名的 HMAC 计算正确，也不能接受遗漏 requestId 的覆盖列表。
        String unsignedRequestIdInput = input.replace(" \"x-sm-request-id\"", "");
        String unsignedRequestIdBase = base.replace("\n\"x-sm-request-id\": " + requestId, "")
                .replace(input.substring("sm1=".length()), unsignedRequestIdInput.substring("sm1=".length()));
        String unsignedRequestIdSignature = "sm1=:" + Base64.getEncoder().encodeToString(
                mac.doFinal(unsignedRequestIdBase.getBytes(StandardCharsets.UTF_8))) + ":";
        assertThrows(BizException.class, () -> verifier.verify(body, "POST", path, query, contentType,
                keyId, created, nonce, requestId, digest, unsignedRequestIdInput, unsignedRequestIdSignature, secret));
    }

    @Test
    void legacyLowercaseMethodAndMissingNonceParameterAreRejected() throws Exception {
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
        byte[] secret = "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8);
        String keyId = "sm_test_key";
        String nonce = "nonce-123456";
        long created = 1788163200L;
        String path = "/openapi/test";
        String digest = "sha-256=:" + Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(body)) + ":";
        String legacyInput = "sm1=(\"@method\" \"@path\" \"content-digest\" \"x-sm-key-id\" "
                + "\"x-sm-timestamp\" \"x-sm-nonce\");created=" + created + ";keyid=\"" + keyId
                + "\";alg=\"hmac-sha256\"";
        String legacyBase = "\"@method\": post\n\"@path\": " + path
                + "\n\"content-digest\": " + digest
                + "\n\"x-sm-key-id\": " + keyId
                + "\n\"x-sm-timestamp\": " + created
                + "\n\"x-sm-nonce\": " + nonce
                + "\n\"@signature-params\": " + legacyInput.substring("sm1=".length());
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        String legacySignature = "sm1=:" + Base64.getEncoder().encodeToString(
                mac.doFinal(legacyBase.getBytes(StandardCharsets.UTF_8))) + ":";

        assertThrows(BizException.class, () -> verifier.verify(body, "POST", path, "?",
                "application/json", keyId, created, nonce, "request-123456", digest, legacyInput, legacySignature, secret));
    }
}
