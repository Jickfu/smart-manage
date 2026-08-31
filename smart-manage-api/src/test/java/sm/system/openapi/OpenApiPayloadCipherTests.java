package sm.system.openapi;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenApiPayloadCipherTests {
    private final OpenApiPayloadCipher cipher = new OpenApiPayloadCipher();

    @Test
    void aes256GcmRoundTripAndAadTamperDetection() {
        assertRoundTripAndTamper("AES-256-GCM", 32);
    }

    @Test
    void sm4GcmRoundTripAndAadTamperDetection() {
        assertRoundTripAndTamper("SM4-GCM", 16);
    }

    private void assertRoundTripAndTamper(String algorithm, int keyLength) {
        byte[] key = new byte[keyLength];
        new SecureRandom().nextBytes(key);
        byte[] plaintext = "{\"categoryNumber\":\"device-type\"}".getBytes(StandardCharsets.UTF_8);
        OpenApiAssociatedData associatedData = new OpenApiAssociatedData(
                "1", algorithm, "sm_test_key", "request", "POST",
                "/openapi/sys/base/basic-data/v1/items/query", 1788163200L,
                "nonce-123456", "request-123456");

        OpenApiEncryptedPayload payload = cipher.encrypt(
                plaintext, algorithm, "sm_test_key", key, associatedData);

        assertArrayEquals(plaintext, cipher.decrypt(payload, key, associatedData));
        OpenApiAssociatedData tampered = new OpenApiAssociatedData(
                "1", algorithm, "sm_test_key", "response", "POST",
                "/openapi/sys/base/basic-data/v1/items/query", 1788163200L,
                "nonce-123456", "request-123456");
        assertThrows(BizException.class, () -> cipher.decrypt(payload, key, tampered));
    }
}
