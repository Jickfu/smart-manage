package sm.system.config;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Sm4PropertiesTests {

    @Test
    void acceptsExactlySixteenDecodedBytes() {
        byte[] expected = "smart-manage-dev".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Sm4Properties properties = new Sm4Properties();
        properties.setKeyBase64(Base64.getEncoder().encodeToString(expected));

        properties.afterPropertiesSet();

        assertArrayEquals(expected, properties.getKeyBytes());
    }

    @Test
    void rejectsMissingInvalidAndWrongLengthKeys() {
        Sm4Properties missing = new Sm4Properties();
        assertThrows(IllegalStateException.class, missing::afterPropertiesSet);

        Sm4Properties invalidBase64 = new Sm4Properties();
        invalidBase64.setKeyBase64("not-base64");
        assertThrows(IllegalStateException.class, invalidBase64::afterPropertiesSet);

        Sm4Properties wrongLength = new Sm4Properties();
        wrongLength.setKeyBase64(Base64.getEncoder().encodeToString(new byte[15]));
        assertThrows(IllegalStateException.class, wrongLength::afterPropertiesSet);
    }

    @Test
    void returnsDefensiveKeyCopies() {
        Sm4Properties properties = validProperties();
        byte[] first = properties.getKeyBytes();
        first[0] = 0;

        byte[] second = properties.getKeyBytes();

        org.junit.jupiter.api.Assertions.assertNotEquals(0, second[0]);
    }

    private Sm4Properties validProperties() {
        Sm4Properties properties = new Sm4Properties();
        byte[] key = new byte[16];
        java.util.Arrays.fill(key, (byte) 7);
        properties.setKeyBase64(Base64.getEncoder().encodeToString(key));
        properties.afterPropertiesSet();
        return properties;
    }
}
