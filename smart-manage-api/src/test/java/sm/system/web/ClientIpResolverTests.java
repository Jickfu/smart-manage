package sm.system.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientIpResolverTests {

    @Test
    void untrustedDirectSourceCannotSpoofForwardedHeader() {
        ClientIpResolver resolver = resolver("10.0.0.0/8");
        HttpServletRequest request = request("203.0.113.8", "198.51.100.9");

        assertEquals("203.0.113.8", resolver.resolve(request));
    }

    @Test
    void trustedProxyChainReturnsRightmostUntrustedClient() {
        ClientIpResolver resolver = resolver("10.0.0.0/8", "192.168.0.0/16");
        HttpServletRequest request = request("10.0.0.5", "198.51.100.9, 192.168.1.10");

        assertEquals("198.51.100.9", resolver.resolve(request));
    }

    @Test
    void trustedProxyWithoutForwardedHeaderFallsBackToRemoteAddress() {
        ClientIpResolver resolver = resolver("10.0.0.0/8");
        HttpServletRequest request = request("10.0.0.5", null);

        assertEquals("10.0.0.5", resolver.resolve(request));
    }

    private ClientIpResolver resolver(String... cidrs) {
        TrustedProxyProperties properties = new TrustedProxyProperties();
        properties.setCidrs(List.of(cidrs));
        return new ClientIpResolver(properties);
    }

    private HttpServletRequest request(String remoteAddress, String forwardedFor) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(remoteAddress);
        when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedFor);
        return request;
    }
}
