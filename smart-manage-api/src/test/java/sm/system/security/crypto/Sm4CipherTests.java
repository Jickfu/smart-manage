package sm.system.security.crypto;

import org.junit.jupiter.api.Test;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sm4CipherTests {

    @Test
    void encryptsAndDecryptsWithConfiguredKeyAndRandomIv() {
        Sm4Properties properties = new Sm4Properties();
        properties.setKeyBase64(Base64.getEncoder().encodeToString(new byte[16]));
        properties.afterPropertiesSet();
        Sm4Cipher helper = new Sm4Cipher(properties);

        String firstCipher = helper.encrypt("ftp-password");
        String secondCipher = helper.encrypt("ftp-password");

        assertNotEquals(firstCipher, secondCipher);
        assertTrue(firstCipher.startsWith("sm4-gcm:v1:"));
        assertEquals("ftp-password", helper.decrypt(firstCipher));
        assertEquals("ftp-password", helper.decrypt(secondCipher));
    }

    @Test
    void rejectsTamperedCiphertext() {
        Sm4Properties properties = new Sm4Properties();
        properties.setKeyBase64(Base64.getEncoder().encodeToString(new byte[16]));
        properties.afterPropertiesSet();
        Sm4Cipher helper = new Sm4Cipher(properties);
        String cipherText = helper.encrypt("s3-secret");
        char replacement = cipherText.endsWith("A") ? 'B' : 'A';

        assertThrows(RuntimeException.class,
                () -> helper.decrypt(cipherText.substring(0, cipherText.length() - 1) + replacement));
    }
}
