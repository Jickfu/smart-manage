package sm.system.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorUtilTests {

    @Test
    void generatedPasswordAlwaysContainsAllRequiredCharacterTypes() {
        for (int count = 0; count < 1000; count++) {
            String password = PasswordGeneratorUtil.generate(12);

            assertEquals(12, password.length());
            assertTrue(password.chars().anyMatch(Character::isUpperCase));
            assertTrue(password.chars().anyMatch(Character::isLowerCase));
            assertTrue(password.chars().anyMatch(Character::isDigit));
            assertTrue(password.chars().anyMatch(character -> "!@#$%^&*()-_=+".indexOf(character) >= 0));
        }
    }

    @Test
    void minimumLengthStillContainsAllRequiredCharacterTypes() {
        String password = PasswordGeneratorUtil.generate(4);

        assertEquals(4, password.length());
        assertTrue(password.chars().anyMatch(Character::isUpperCase));
        assertTrue(password.chars().anyMatch(Character::isLowerCase));
        assertTrue(password.chars().anyMatch(Character::isDigit));
        assertTrue(password.chars().anyMatch(character -> "!@#$%^&*()-_=+".indexOf(character) >= 0));
    }

    @Test
    void lengthBelowFourIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PasswordGeneratorUtil.generate(3));
    }
}
