package sm.domain.sys.base.user.constant;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserThemeColorTests {

    @Test
    void shouldNormalizeSupportedColor() {
        assertEquals("#276FF5", UserThemeColor.normalizeRequired(" #276ff5 "));
    }

    @Test
    void shouldRejectArbitraryColor() {
        assertThrows(BizException.class, () -> UserThemeColor.normalizeRequired("#1677FF"));
    }
}
