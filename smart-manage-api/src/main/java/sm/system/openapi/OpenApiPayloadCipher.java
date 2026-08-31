package sm.system.openapi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** OpenAPI 报文级 AES-256-GCM/SM4-GCM 认证加密。 */
@Component
public class OpenApiPayloadCipher {
    private static final Provider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 16;

    public byte[] decrypt(OpenApiEncryptedPayload payload, byte[] key, OpenApiAssociatedData associatedData) {
        validateEnvelope(payload, associatedData, key);
        try {
            byte[] ciphertext = decode(payload.ciphertext(), "ciphertext");
            byte[] tag = decode(payload.tag(), "tag");
            if (tag.length != TAG_LENGTH) {
                throw new BizException(ResultEnum.PARAM_ERROR, "OpenAPI 认证标签长度不正确");
            }
            byte[] combined = new byte[ciphertext.length + tag.length];
            System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
            System.arraycopy(tag, 0, combined, ciphertext.length, tag.length);
            Cipher cipher = cipher(payload.algorithm());
            cipher.init(Cipher.DECRYPT_MODE, keySpec(payload.algorithm(), key),
                    new GCMParameterSpec(TAG_LENGTH * Byte.SIZE, decodeIv(payload.iv())));
            cipher.updateAAD(associatedData.bytes());
            return cipher.doFinal(combined);
        } catch (BizException exception) {
            throw exception;
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "OpenAPI 报文认证失败");
        }
    }

    public OpenApiEncryptedPayload encrypt(byte[] plaintext, String algorithm, String keyId,
                                           byte[] key, OpenApiAssociatedData associatedData) {
        validateKey(algorithm, key);
        byte[] iv = new byte[IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        try {
            Cipher cipher = cipher(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec(algorithm, key),
                    new GCMParameterSpec(TAG_LENGTH * Byte.SIZE, iv));
            cipher.updateAAD(associatedData.bytes());
            byte[] combined = cipher.doFinal(plaintext);
            byte[] ciphertext = Arrays.copyOf(combined, combined.length - TAG_LENGTH);
            byte[] tag = Arrays.copyOfRange(combined, combined.length - TAG_LENGTH, combined.length);
            Base64.Encoder encoder = Base64.getEncoder();
            return new OpenApiEncryptedPayload("1", algorithm, keyId, encoder.encodeToString(iv),
                    encoder.encodeToString(ciphertext), encoder.encodeToString(tag));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("OpenAPI 报文加密失败", exception);
        }
    }

    private void validateEnvelope(OpenApiEncryptedPayload payload, OpenApiAssociatedData associatedData, byte[] key) {
        if (payload == null || !"1".equals(payload.version())
                || !payload.version().equals(associatedData.version())
                || !payload.algorithm().equals(associatedData.algorithm())
                || !payload.keyId().equals(associatedData.keyId())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "OpenAPI 加密信封元数据不一致");
        }
        validateKey(payload.algorithm(), key);
    }

    private void validateKey(String algorithm, byte[] key) {
        int requiredLength = switch (algorithm) {
            case "AES-256-GCM" -> 32;
            case "SM4-GCM" -> 16;
            default -> throw new BizException(ResultEnum.PARAM_ERROR, "OpenAPI 加密算法不受支持");
        };
        if (key == null || key.length != requiredLength) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "OpenAPI 报文密钥长度不正确");
        }
    }

    private Cipher cipher(String algorithm) throws GeneralSecurityException {
        return "SM4-GCM".equals(algorithm)
                ? Cipher.getInstance("SM4/GCM/NoPadding", BOUNCY_CASTLE_PROVIDER)
                : Cipher.getInstance("AES/GCM/NoPadding");
    }

    private SecretKeySpec keySpec(String algorithm, byte[] key) {
        return new SecretKeySpec(key, "SM4-GCM".equals(algorithm) ? "SM4" : "AES");
    }

    private byte[] decodeIv(String value) {
        byte[] iv = decode(value, "iv");
        if (iv.length != IV_LENGTH) {
            throw new BizException(ResultEnum.PARAM_ERROR, "OpenAPI IV 长度不正确");
        }
        return iv;
    }

    private byte[] decode(String value, String field) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (RuntimeException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "OpenAPI " + field + " 不是合法 Base64");
        }
    }
}
