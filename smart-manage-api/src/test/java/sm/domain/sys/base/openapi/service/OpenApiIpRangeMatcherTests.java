package sm.domain.sys.base.openapi.service;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiIpRangeMatcherTests {
    @Test
    void matchesIpv4AndIpv6CidrsWithoutResolvingHostNames() {
        assertTrue(OpenApiIpRangeMatcher.matches("10.10.2.3", List.of("10.10.0.0/16")));
        assertFalse(OpenApiIpRangeMatcher.matches("10.11.2.3", List.of("10.10.0.0/16")));
        assertTrue(OpenApiIpRangeMatcher.matches("2001:db8::8", List.of("2001:db8::/32")));
        assertThrows(BizException.class, () -> OpenApiIpRangeMatcher.normalize("localhost"));
    }
}
