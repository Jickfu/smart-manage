package sm.system.helper;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.SM4;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sm.system.config.Sm4Properties;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.nio.charset.StandardCharsets;
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
public class SM4Helper {

    private static final int IV_LENGTH = 16;

    private final Sm4Properties sm4Properties;

    /**
     * SM4/CBC 加密，随机 IV 拼接在密文前
     *
     * @param plainText 明文字符串
     * @return Base64(IV + 密文)
     */
    public String encrypt(String plainText) {
        byte[] keyBytes = sm4Properties.getKeyBytes();
        byte[] ivBytes = generateIv();
        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);

        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, keyBytes, ivBytes);
        byte[] cipherBytes = sm4.encrypt(plainBytes);

        // IV + 密文 拼接后 Base64 编码
        byte[] result = new byte[IV_LENGTH + cipherBytes.length];
        System.arraycopy(ivBytes, 0, result, 0, IV_LENGTH);
        System.arraycopy(cipherBytes, 0, result, IV_LENGTH, cipherBytes.length);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * SM4/CBC 解密
     *
     * @param cipherBase64 Base64 编码的密文（IV + 密文）
     * @return 明文字符串
     */
    public String decrypt(String cipherBase64) {
        byte[] keyBytes = sm4Properties.getKeyBytes();
        byte[] rawBytes;
        try {
            rawBytes = Base64.getDecoder().decode(cipherBase64);
        } catch (IllegalArgumentException e) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密文 Base64 解码失败");
        }

        if (rawBytes.length < IV_LENGTH + 1) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密文长度不足，无法解密");
        }

        // 提取 IV 和密文
        byte[] ivBytes = new byte[IV_LENGTH];
        byte[] cipherBytes = new byte[rawBytes.length - IV_LENGTH];
        System.arraycopy(rawBytes, 0, ivBytes, 0, IV_LENGTH);
        System.arraycopy(rawBytes, IV_LENGTH, cipherBytes, 0, cipherBytes.length);

        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, keyBytes, ivBytes);
        byte[] plainBytes = sm4.decrypt(cipherBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    /** 生成随机 16 字节 IV */
    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
