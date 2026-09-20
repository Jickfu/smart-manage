package sm.system.json.masking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaskingTypeTests {

    @Test
    void masksPhoneEmailAndShortValuesConservatively() {
        assertEquals("138****1234", MaskingType.PHONE.mask("13812341234"));
        assertEquals("a***@example.com", MaskingType.EMAIL.mask("alice@example.com"));
        assertEquals("**", MaskingType.GENERIC.mask("ab"));
        assertNull(MaskingType.REDACT.mask(null));
    }
}
