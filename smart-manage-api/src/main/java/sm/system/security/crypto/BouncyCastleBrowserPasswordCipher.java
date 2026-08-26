package sm.system.security.crypto;

import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;

/** 直接使用 Bouncy Castle 实现浏览器 SM2 C1C3C2 密文解密。 */
@Component
@RequiredArgsConstructor
public class BouncyCastleBrowserPasswordCipher implements BrowserPasswordCipher {

    private static final int C1_WITHOUT_PREFIX_BYTES = 64;
    private static final int C3_BYTES = 32;
    private static final int MINIMUM_CIPHERTEXT_HEX_LENGTH = (C1_WITHOUT_PREFIX_BYTES + C3_BYTES) * 2;

    private final Sm2Properties properties;

    @Override
    public String decrypt(String ciphertext) {
        byte[] browserCiphertext = parseBrowserCiphertext(ciphertext);
        byte[] engineCiphertext = new byte[browserCiphertext.length + 1];
        engineCiphertext[0] = 0x04;
        System.arraycopy(browserCiphertext, 0, engineCiphertext, 1, browserCiphertext.length);

        byte[] plaintext = null;
        try {
            SM2Engine engine = new SM2Engine(new SM3Digest(), SM2Engine.Mode.C1C3C2);
            engine.init(false, properties.privateKeyParameters());
            plaintext = engine.processBlock(engineCiphertext, 0, engineCiphertext.length);
            return decodeUtf8(plaintext);
        } catch (InvalidCipherTextException | IllegalArgumentException exception) {
            throw new Sm2CiphertextException("SM2 密文无法通过完整性校验", exception);
        } finally {
            Arrays.fill(browserCiphertext, (byte) 0);
            Arrays.fill(engineCiphertext, (byte) 0);
            if (plaintext != null) {
                Arrays.fill(plaintext, (byte) 0);
            }
        }
    }

    @Override
    public String publicKey() {
        return properties.publicKeyHex();
    }

    private byte[] parseBrowserCiphertext(String ciphertext) {
        if (ciphertext == null || ciphertext.length() < MINIMUM_CIPHERTEXT_HEX_LENGTH
                || (ciphertext.length() & 1) != 0) {
            throw new Sm2CiphertextException("SM2 密文格式无效");
        }
        try {
            return HexFormat.of().parseHex(ciphertext);
        } catch (IllegalArgumentException exception) {
            throw new Sm2CiphertextException("SM2 密文格式无效", exception);
        }
    }

    private String decodeUtf8(byte[] plaintext) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(plaintext))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new Sm2CiphertextException("SM2 明文不是有效的 UTF-8 文本", exception);
        }
    }
}
