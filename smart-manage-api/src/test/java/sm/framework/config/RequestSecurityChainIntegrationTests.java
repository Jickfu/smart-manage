package sm.framework.config;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.filter.SaTokenContextFilterForJakartaServlet;
import cn.dev33.satoken.spring.SaTokenContextRegister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import sm.system.interceptor.TraceIdInterceptor;
import sm.system.response.Result;
import sm.framework.security.BrowserRequestSecurity;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证实际 Servlet Filter、MVC Interceptor 与统一响应的关键组合，不依赖数据库或 Redis。 */
class RequestSecurityChainIntegrationTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        new SaTokenContextRegister();
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of());
        BrowserRequestSecurity browserRequestSecurity = mock(BrowserRequestSecurity.class);
        SaTokenConfig config = new SaTokenConfig(
                handlerMapping, JsonMapper.builder().build(), browserRequestSecurity);
        ReflectionTestUtils.setField(config, "noNeedLogin", new String[]{"/public-test"});
        SaServletFilter filter = config.getSaServletFilter();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(new SaTokenContextFilterForJakartaServlet(), filter)
                .addInterceptors(new TraceIdInterceptor())
                .build();
    }

    @Test
    void publicRequestPassesFilterAndCarriesTraceId() throws Exception {
        mockMvc.perform(get("/public-test").header("traceId", "trace-test-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").value("trace-test-1"));
    }

    @Test
    void protectedRequestWithoutLoginIsRejectedByFilter() throws Exception {
        mockMvc.perform(get("/protected-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(100401));
    }

    @RestController
    private static class TestController {
        @GetMapping("/public-test")
        Result<String> publicEndpoint() {
            return Result.success("ok");
        }

        @GetMapping("/protected-test")
        Result<String> protectedEndpoint() {
            return Result.success("secret");
        }
    }
}
