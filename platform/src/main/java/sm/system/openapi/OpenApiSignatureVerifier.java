package sm.system.openapi;

import com.authlete.hms.ComponentIdentifier;
import com.authlete.hms.SignatureBase;
import com.authlete.hms.SignatureBaseBuilder;
import com.authlete.hms.SignatureEntry;
import com.authlete.hms.SignatureField;
import com.authlete.hms.SignatureInputField;
import com.authlete.hms.SignatureMetadata;
import com.authlete.hms.SignatureMetadataParameters;
import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/** 严格 RFC 9421 的固定 HMAC-SHA256 应用配置文件。 */
@Component
public class OpenApiSignatureVerifier {
    private static final String SIGNATURE_LABEL = "sm1";
    private static final String ALGORITHM = "hmac-sha256";
    private static final List<ComponentIdentifier> COVERED_COMPONENTS = List.of(
            new ComponentIdentifier("@method"),
            new ComponentIdentifier("@path"),
            new ComponentIdentifier("@query"),
            new ComponentIdentifier("content-type"),
            new ComponentIdentifier("content-digest"),
            new ComponentIdentifier("x-sm-key-id"),
            new ComponentIdentifier("x-sm-timestamp"),
            new ComponentIdentifier("x-sm-nonce"));

    public void verify(byte[] rawBody, String method, String path, String query, String contentType,
                       String keyId, long created, String nonce, String contentDigest, String signatureInput,
                       String signature, byte[] secret) {
        String expectedDigest = "sha-256=:" + Base64.getEncoder().encodeToString(sha256(rawBody)) + ":";
        if (!MessageDigest.isEqual(expectedDigest.getBytes(StandardCharsets.US_ASCII),
                safe(contentDigest).getBytes(StandardCharsets.US_ASCII))) {
            reject();
        }
        try {
            SignatureEntry entry = parseSingleSignature(signatureInput, signature);
            SignatureMetadata expectedMetadata = expectedMetadata(created, keyId, nonce);
            if (!entry.getMetadata().equals(expectedMetadata)
                    || !entry.getMetadata().serialize().equals(expectedMetadata.serialize())) {
                reject();
            }
            SignatureBase signatureBase = new SignatureBaseBuilder((metadata, identifier) -> switch (
                    identifier.getComponentName()) {
                case "@method" -> method;
                case "@path" -> path;
                case "@query" -> query;
                case "content-type" -> contentType;
                case "content-digest" -> expectedDigest;
                case "x-sm-key-id" -> keyId;
                case "x-sm-timestamp" -> Long.toString(created);
                case "x-sm-nonce" -> nonce;
                default -> null;
            }).build(entry.getMetadata());
            boolean valid = signatureBase.verify((base, actual) ->
                    MessageDigest.isEqual(hmac(base, secret), actual), entry.getSignature());
            if (!valid) {
                reject();
            }
        } catch (SignatureException | IllegalArgumentException | IllegalStateException exception) {
            reject();
        }
    }

    private SignatureEntry parseSingleSignature(String signatureInput, String signature)
            throws SignatureException {
        SignatureInputField inputField = SignatureInputField.parse(signatureInput);
        SignatureField signatureField = SignatureField.parse(signature);
        Map<String, SignatureEntry> entries = SignatureEntry.scan(signatureField, inputField);
        if (entries == null || entries.size() != 1 || !entries.containsKey(SIGNATURE_LABEL)) {
            reject();
        }
        return entries.get(SIGNATURE_LABEL);
    }

    private SignatureMetadata expectedMetadata(long created, String keyId, String nonce) {
        SignatureMetadataParameters parameters = new SignatureMetadataParameters()
                .setCreated(created)
                .setKeyid(keyId)
                .setNonce(nonce)
                .setAlg(ALGORITHM);
        return new SignatureMetadata(COVERED_COMPONENTS, parameters);
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void reject() {
        throw new BizException(ResultEnum.UNAUTHORIZED, "OpenAPI 请求认证失败");
    }
}
