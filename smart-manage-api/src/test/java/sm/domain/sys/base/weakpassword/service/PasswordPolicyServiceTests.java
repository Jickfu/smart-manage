package sm.domain.sys.base.weakpassword.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.util.WeakPasswordUtil;
import sm.system.exception.BizException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordPolicyServiceTests {
    private final WeakPasswordMapper mapper = mock(WeakPasswordMapper.class);
    private final PasswordPolicyService policy = new PasswordPolicyService(mapper);

    @Test
    void enforcesCodePointBoundariesWithoutCompositionRulesOrTrimming() {
        assertThrows(BizException.class, () -> policy.validate("界".repeat(14), "tester"));
        assertDoesNotThrow(() -> policy.validate("界".repeat(15), "tester"));
        assertDoesNotThrow(() -> policy.validate("😀".repeat(64), "tester"));
        assertThrows(BizException.class, () -> policy.validate("😀".repeat(65), "tester"));
        assertDoesNotThrow(() -> policy.validate("quiet river under moonlight", "tester"));
        String spaced = "  river garden  ";
        assertDoesNotThrow(() -> policy.validate(spaced, "tester"));
        verify(mapper).containsDigest(WeakPasswordUtil.digest(spaced));
    }

    @Test
    void rejectsBlanksInvalidUnicodeAndControlCharactersBeforeDatabaseAccess() {
        for (String password : new String[]{"", " ".repeat(15), "\u00a0".repeat(15),
                "\ufeff".repeat(15), "test\nlongpassword", "\ud800".repeat(15)}) {
            assertThrows(BizException.class, () -> policy.validate(password, "tester"));
        }
        assertThrows(BizException.class, () -> policy.validate(null, "tester"));
        verifyNoInteractions(mapper);
    }

    @Test
    void blocksFullNormalizedWordsButAllowsLongPhrasesContainingThem() {
        String blocked = "café beside river";
        when(mapper.containsDigest(WeakPasswordUtil.digest(blocked))).thenReturn(true);
        assertThrows(BizException.class, () -> policy.validate("CAFE\u0301 BESIDE RIVER", "tester"));
        assertDoesNotThrow(() -> policy.validate("café beside river under moonlight", "tester"));
    }

    @Test
    void blocksAccountAndProductSimpleVariantsWithoutSubstringBlocking() {
        assertThrows(BizException.class, () -> policy.validate("Administrator2026!", "administrator"));
        assertThrows(BizException.class, () -> policy.validate("Smart-Manage2026!", "tester"));
        assertDoesNotThrow(() -> policy.validate("administrator beside quiet river", "administrator"));
    }

    @Test
    void databaseFailureCannotSilentlyDisableBlacklist() {
        when(mapper.containsDigest(anyString())).thenThrow(new IllegalStateException("数据库不可用"));
        assertThrows(IllegalStateException.class, () -> policy.validate("quiet river under moonlight", "tester"));
    }
}
