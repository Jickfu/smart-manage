package sm.system.helper;

import org.junit.jupiter.api.Test;
import sm.system.config.Sm4Properties;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SM4HelperTests {

    @Test
    void encryptsAndDecryptsWithConfiguredKeyAndRandomIv() {
        Sm4Properties properties = new Sm4Properties();
        properties.setKeyBase64(Base64.getEncoder().encodeToString(new byte[16]));
        properties.afterPropertiesSet();
        SM4Helper helper = new SM4Helper(properties);

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
        SM4Helper helper = new SM4Helper(properties);
        String cipherText = helper.encrypt("s3-secret");
        char replacement = cipherText.endsWith("A") ? 'B' : 'A';

        assertThrows(RuntimeException.class,
                () -> helper.decrypt(cipherText.substring(0, cipherText.length() - 1) + replacement));
    }
}
