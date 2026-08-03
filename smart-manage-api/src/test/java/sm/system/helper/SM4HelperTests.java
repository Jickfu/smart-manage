package sm.system.helper;

import org.junit.jupiter.api.Test;
import sm.system.config.Sm4Properties;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        assertEquals("ftp-password", helper.decrypt(firstCipher));
        assertEquals("ftp-password", helper.decrypt(secondCipher));
    }
}
