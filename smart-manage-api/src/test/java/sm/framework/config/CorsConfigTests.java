package sm.framework.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CorsConfigTests {

	private final CorsFilter corsFilter = createCorsFilter();

	@Test
	void localhostDevelopmentOriginIsAllowed() throws Exception {
		MockHttpServletResponse response = preflight("http://localhost:8000");

		assertEquals(200, response.getStatus());
		assertEquals("http://localhost:8000", response.getHeader("Access-Control-Allow-Origin"));
	}

	@Test
	void loopbackDevelopmentOriginIsAllowed() throws Exception {
		MockHttpServletResponse response = preflight("http://127.0.0.1:8000");

		assertEquals(200, response.getStatus());
		assertEquals("http://127.0.0.1:8000", response.getHeader("Access-Control-Allow-Origin"));
	}

	@Test
	void unconfiguredOriginIsRejected() throws Exception {
		MockHttpServletResponse response = preflight("http://example.com");

		assertEquals(403, response.getStatus());
	}

	private MockHttpServletResponse preflight(String origin) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/smart-manage-api/sys/base/login");
		request.addHeader("Origin", origin);
		request.addHeader("Access-Control-Request-Method", "POST");
		request.addHeader("Access-Control-Request-Headers", "content-type,sm-csrf-token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		corsFilter.doFilter(request, response, mock(FilterChain.class));
		return response;
	}

	private CorsFilter createCorsFilter() {
		CorsProperties properties = new CorsProperties(List.of("http://localhost:*", "http://127.0.0.1:*"));
		CorsConfig config = new CorsConfig(properties);
		FilterRegistrationBean<CorsFilter> registration = config.corsFilter();
		return registration.getFilter();
	}
}
