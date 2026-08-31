package sm.system.openapi;

import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** RFC 9421 风格的固定 HMAC-SHA256 应用配置文件。 */
@Component
public class OpenApiSignatureVerifier {
    public void verify(byte[] rawBody, String method, String path, String keyId, long created,
                       String nonce, String contentDigest, String signatureInput,
                       String signature, byte[] secret) {
        String expectedDigest = "sha-256=:" + Base64.getEncoder().encodeToString(sha256(rawBody)) + ":";
        if (!MessageDigest.isEqual(expectedDigest.getBytes(StandardCharsets.US_ASCII),
                safe(contentDigest).getBytes(StandardCharsets.US_ASCII))) {
            reject();
        }
        String expectedInput = "sm1=(\"@method\" \"@path\" \"content-digest\" \"x-sm-key-id\" "
                + "\"x-sm-timestamp\" \"x-sm-nonce\");created=" + created + ";keyid=\"" + keyId
                + "\";alg=\"hmac-sha256\"";
        if (!expectedInput.equals(signatureInput)) {
            reject();
        }
        String signatureBase = "\"@method\": " + method.toLowerCase() + "\n"
                + "\"@path\": " + path + "\n"
                + "\"content-digest\": " + expectedDigest + "\n"
                + "\"x-sm-key-id\": " + keyId + "\n"
                + "\"x-sm-timestamp\": " + created + "\n"
                + "\"x-sm-nonce\": " + nonce + "\n"
                + "\"@signature-params\": " + expectedInput.substring("sm1=".length());
        byte[] expected = hmac(signatureBase.getBytes(StandardCharsets.UTF_8), secret);
        byte[] actual = parseSignature(signature);
        if (!MessageDigest.isEqual(expected, actual)) {
            reject();
        }
    }

    private byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private byte[] hmac(byte[] value, byte[] secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private byte[] parseSignature(String value) {
        if (value == null || !value.startsWith("sm1=:") || !value.endsWith(":")) {
            reject();
        }
        try {
            return Base64.getDecoder().decode(value.substring(5, value.length() - 1));
        } catch (RuntimeException exception) {
            reject();
            return new byte[0];
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void reject() {
        throw new BizException(ResultEnum.UNAUTHORIZED, "OpenAPI 请求认证失败");
    }
}
