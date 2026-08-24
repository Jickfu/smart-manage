package sm.system.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sm.framework.config.CorsProperties;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.CsrfTokenManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class BrowserRequestSecurityTests {
    private final CsrfTokenManager csrfTokenManager = mock(CsrfTokenManager.class);
    private final BrowserRequestSecurity security = new BrowserRequestSecurity(
            new CorsProperties(List.of("http://localhost:*")), csrfTokenManager);

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void safeRequestNeedsNeitherOriginNorCsrfToken() {
        bindRequest(new MockHttpServletRequest("GET", "/sys/base/session"));

        security.validateOrigin();
        security.validateCsrfToken();

        verifyNoInteractions(csrfTokenManager);
    }

    @Test
    void configuredOriginAndCsrfHeaderAreAcceptedForWriteRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sys/base/user/current/theme");
        request.addHeader("Origin", "http://localhost:8000");
        request.addHeader(CsrfTokenManager.HEADER_NAME, "0123456789abcdef0123456789abcdef");
        bindRequest(request);

        security.validateOrigin();
        security.validateCsrfToken();

        verify(csrfTokenManager).validateCurrentToken("0123456789abcdef0123456789abcdef");
    }

    @Test
    void missingOriginIsRejectedForWriteRequest() {
        bindRequest(new MockHttpServletRequest("POST", "/sys/base/login"));

        BizException exception = assertThrows(BizException.class, security::validateOrigin);

        assertEquals(ResultEnum.CSRF_TOKEN_INVALID.getCode(), exception.getCode());
    }

    @Test
    void unconfiguredOriginIsRejectedForWriteRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sys/base/login");
        request.addHeader("Origin", "http://example.com");
        bindRequest(request);

        BizException exception = assertThrows(BizException.class, security::validateOrigin);

        assertEquals(ResultEnum.CSRF_TOKEN_INVALID.getCode(), exception.getCode());
    }

    private void bindRequest(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
