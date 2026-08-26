package sm.system.security.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Sm2PropertiesTests {

    static final String PRIVATE_KEY =
            "00dc349799de9b5a3beef8d3d77aa7e3655c5542a10c395cc86670580b6250dfeb";
    static final String PUBLIC_KEY =
            "0405102042204d15e802522ea09eb609358e4c72295e0ca44727eef75faac9afb3eeaa3e9df5af87e205e6ce5cb351e4fb30c37602f32aad969d2b222f05f8fa99";

    @Test
    void acceptsExistingPositiveIntegerPrivateKeyEncodingAndMatchingPublicKey() {
        Sm2Properties properties = validProperties();

        assertEquals(PUBLIC_KEY, properties.publicKeyHex());
    }

    @Test
    void rejectsMissingMalformedAndMismatchedKeys() {
        Sm2Properties missingPrivateKey = new Sm2Properties();
        missingPrivateKey.setPublicKey(PUBLIC_KEY);
        assertThrows(IllegalStateException.class, missingPrivateKey::afterPropertiesSet);

        Sm2Properties malformedPublicKey = new Sm2Properties();
        malformedPublicKey.setPrivateKey(PRIVATE_KEY);
        malformedPublicKey.setPublicKey("not-hex");
        assertThrows(IllegalStateException.class, malformedPublicKey::afterPropertiesSet);

        Sm2Properties mismatchedKeys = new Sm2Properties();
        mismatchedKeys.setPrivateKey("01".repeat(32));
        mismatchedKeys.setPublicKey(PUBLIC_KEY);
        assertThrows(IllegalStateException.class, mismatchedKeys::afterPropertiesSet);
    }

    static Sm2Properties validProperties() {
        Sm2Properties properties = new Sm2Properties();
        properties.setPrivateKey(PRIVATE_KEY);
        properties.setPublicKey(PUBLIC_KEY);
        properties.afterPropertiesSet();
        return properties;
    }
}
