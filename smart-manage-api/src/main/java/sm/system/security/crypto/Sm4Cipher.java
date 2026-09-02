package sm.system.security.crypto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SM4 加解密工具，密钥由部署配置注入。
 *
 * @author Chekfu
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Sm4Cipher {

    private static final String CIPHERTEXT_PREFIX = "sm4-gcm:v1:";
    private static final String TRANSFORMATION = "SM4/GCM/NoPadding";
    private static final int NONCE_LENGTH = 12;
    private static final int AUTH_TAG_BITS = 128;
    private static final Provider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Sm4Properties sm4Properties;

    /**
     * SM4/GCM 认证加密，随机 nonce 拼接在密文前。
     *
     * @param plainText 明文字符串
     * @return 带算法版本前缀的 Base64(nonce + 密文 + 认证标签)
     */
    public String encrypt(String plainText) {
        byte[] keyBytes = sm4Properties.getKeyBytes();
        byte[] nonceBytes = generateNonce();
        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BOUNCY_CASTLE_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "SM4"),
                    new GCMParameterSpec(AUTH_TAG_BITS, nonceBytes));
            byte[] cipherBytes = cipher.doFinal(plainBytes);
            byte[] result = new byte[NONCE_LENGTH + cipherBytes.length];
            System.arraycopy(nonceBytes, 0, result, 0, NONCE_LENGTH);
            System.arraycopy(cipherBytes, 0, result, NONCE_LENGTH, cipherBytes.length);
            return CIPHERTEXT_PREFIX + Base64.getEncoder().encodeToString(result);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("SM4/GCM 加密失败", exception);
        }
    }

    /**
     * SM4/GCM 认证解密；不兼容没有版本标识的旧 CBC 密文。
     *
     * @param cipherText 带算法版本前缀的认证密文
     * @return 明文字符串
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || !cipherText.startsWith(CIPHERTEXT_PREFIX)) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "密文格式版本不受支持，请重新保存敏感配置");
        }
        byte[] keyBytes = sm4Properties.getKeyBytes();
        byte[] rawBytes;
        try {
            rawBytes = Base64.getDecoder().decode(cipherText.substring(CIPHERTEXT_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密文 Base64 解码失败");
        }

        if (rawBytes.length < NONCE_LENGTH + 16) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密文长度不足，无法解密");
        }
        byte[] nonceBytes = new byte[NONCE_LENGTH];
        byte[] cipherBytes = new byte[rawBytes.length - NONCE_LENGTH];
        System.arraycopy(rawBytes, 0, nonceBytes, 0, NONCE_LENGTH);
        System.arraycopy(rawBytes, NONCE_LENGTH, cipherBytes, 0, cipherBytes.length);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BOUNCY_CASTLE_PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "SM4"),
                    new GCMParameterSpec(AUTH_TAG_BITS, nonceBytes));
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "密文认证失败或密钥不匹配");
        }
    }

    /** 每条密文使用独立随机 nonce，避免同一密钥下重复。 */
    private byte[] generateNonce() {
        byte[] nonce = new byte[NONCE_LENGTH];
        SECURE_RANDOM.nextBytes(nonce);
        return nonce;
    }
}
