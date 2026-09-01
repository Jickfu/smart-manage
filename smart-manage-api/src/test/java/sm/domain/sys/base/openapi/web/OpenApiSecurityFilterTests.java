package sm.domain.sys.base.openapi.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class OpenApiSecurityFilterTests {
    private final OpenApiSecurityFilter filter = new OpenApiSecurityFilter(
            mock(sm.system.openapi.OpenApiOperationRegistry.class),
            mock(sm.domain.sys.base.openapi.service.OpenApiRuntimeAccessService.class),
            mock(sm.domain.sys.base.openapi.service.OpenApiNonceService.class),
            mock(sm.system.openapi.OpenApiSignatureVerifier.class),
            mock(sm.system.openapi.OpenApiPayloadCipher.class),
            mock(sm.domain.sys.base.openapi.service.OpenApiInvocationRecorder.class),
            mock(sm.system.web.ClientIpResolver.class),
            mock(tools.jackson.databind.json.JsonMapper.class));

    @Test
    void queryUsesRawValueAndMissingQueryUsesQuestionMark() {
        MockHttpServletRequest withoutQuery = new MockHttpServletRequest("POST", "/openapi/test");
        assertEquals("?", filter.requestQuery(withoutQuery));

        MockHttpServletRequest withQuery = new MockHttpServletRequest("POST", "/openapi/test");
        withQuery.setQueryString("status=ENABLED&value=%2F+raw");
        assertEquals("?status=ENABLED&value=%2F+raw", filter.requestQuery(withQuery));
    }

    @Test
    void contentTypeOnlyAcceptsExactApplicationJson() {
        assertDoesNotThrow(() -> filter.validateContentType("application/json"));
        assertThrows(BizException.class, () -> filter.validateContentType(null));
        assertThrows(BizException.class,
                () -> filter.validateContentType("application/json;charset=UTF-8"));
    }

    @Test
    void timestampWindowRejectsOverflowValuesAndAcceptsInclusiveBoundaries() {
        long now = 1_788_163_200L;

        assertTrue(OpenApiSecurityFilter.isWithinClockSkew(now - 300, now));
        assertTrue(OpenApiSecurityFilter.isWithinClockSkew(now + 300, now));
        assertFalse(OpenApiSecurityFilter.isWithinClockSkew(now - 301, now));
        assertFalse(OpenApiSecurityFilter.isWithinClockSkew(now + 301, now));
        assertFalse(OpenApiSecurityFilter.isWithinClockSkew(Long.MIN_VALUE, now));
        assertFalse(OpenApiSecurityFilter.isWithinClockSkew(Long.MAX_VALUE, now));
    }
}
