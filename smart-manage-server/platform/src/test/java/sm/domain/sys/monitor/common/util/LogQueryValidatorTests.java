package sm.domain.sys.monitor.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogQueryValidatorTests {
    @Test
    void restrictedBeginTimeCannotBeRelaxedByClient() {
        LocalDateTime cutoff = LocalDateTime.of(2026, 8, 8, 12, 0);

        assertEquals(cutoff, LogQueryValidator.resolveRestrictedBeginTime(null, cutoff));
        assertEquals(cutoff, LogQueryValidator.resolveRestrictedBeginTime(cutoff.minusDays(30), cutoff));
        assertEquals(cutoff.plusDays(1),
                LogQueryValidator.resolveRestrictedBeginTime(cutoff.plusDays(1), cutoff));
    }
}
