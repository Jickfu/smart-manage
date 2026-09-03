package sm.system.config;

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
import sm.system.security.web.BrowserRequestSecurity;
import sm.system.security.config.SaTokenConfig;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证实际 Servlet Filter、MVC Interceptor 与统一响应的关键组合，不依赖数据库或 Redis。 */
class RequestSecurityChainIntegrationTests {

    private MockMvc mockMvc;

    @Test
    void persistentGenerationGuardRejectsOldSessionAndPreservesDatabaseFailureMeaning() throws Exception {
        new SaTokenContextRegister();
        var handlerMapping = mock(RequestMappingHandlerMapping.class);
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of());
        var userMapper = mock(sm.domain.sys.base.user.mapper.UserMapper.class);
        var guard = new sm.system.security.SessionCredentialGuard(
                new sm.domain.sys.base.user.service.UserSessionStateVerifier(userMapper));
        var config = new SaTokenConfig(handlerMapping, JsonMapper.builder().build(),
                mock(BrowserRequestSecurity.class), guard);
        ReflectionTestUtils.setField(config, "noNeedLogin", new String[]{"/public-test"});
        var protectedMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(new SaTokenContextFilterForJakartaServlet(), config.getSaServletFilter()).build();
        var session = mock(cn.dev33.satoken.session.SaSession.class);
        var logic = mock(cn.dev33.satoken.stp.StpLogic.class);
        when(logic.getTokenSession(false)).thenReturn(session);
        when(session.get(sm.system.security.SessionCredentialGuard.GENERATION_CLAIM)).thenReturn("1");
        var user = new sm.domain.sys.base.user.model.entity.UserEntity();
        user.setEnabled(true);
        user.setCredentialGeneration(2L);
        when(userMapper.selectSecurityState(1L)).thenReturn(user);
        // 仅替换登录存储，实际过滤器、代际守卫和持久状态判定均参与请求。
        try (var authentication = org.mockito.Mockito.mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            authentication.when(cn.dev33.satoken.stp.StpUtil::getStpLogic).thenReturn(logic);
            authentication.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            protectedMvc.perform(get("/protected-test")).andExpect(jsonPath("$.code").value(100401));
            when(session.get(sm.system.security.SessionCredentialGuard.GENERATION_CLAIM)).thenReturn("2");
            protectedMvc.perform(get("/protected-test")).andExpect(jsonPath("$.data").value("secret"));
            when(userMapper.selectSecurityState(1L)).thenThrow(new IllegalStateException("database unavailable"));
            protectedMvc.perform(get("/protected-test")).andExpect(jsonPath("$.code").value(100500));
        }
    }

    @BeforeEach
    void setUp() {
        new SaTokenContextRegister();
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of());
        BrowserRequestSecurity browserRequestSecurity = mock(BrowserRequestSecurity.class);
        SaTokenConfig config = new SaTokenConfig(
                handlerMapping, JsonMapper.builder().build(), browserRequestSecurity, mock(sm.system.security.SessionCredentialGuard.class));
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
                .andExpect(jsonPath("$.code").value(100401))
                .andExpect(jsonPath("$.feedbackLevel").value("ERROR"));
    }

    @Test
    void brokenMapperStillReturnsACompleteSafeFailureEnvelope() throws Exception {
        RequestMappingHandlerMapping handlerMapping = mock(RequestMappingHandlerMapping.class);
        when(handlerMapping.getHandlerMethods()).thenReturn(Map.of());
        JsonMapper mapper = mock(JsonMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new IllegalStateException("sensitive detail"));
        SaTokenConfig config = new SaTokenConfig(handlerMapping, mapper, mock(BrowserRequestSecurity.class), mock(sm.system.security.SessionCredentialGuard.class));
        ReflectionTestUtils.setField(config, "noNeedLogin", new String[]{"/public-test"});
        var failingMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(new SaTokenContextFilterForJakartaServlet(), config.getSaServletFilter()).build();
        failingMvc.perform(get("/protected-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(100500))
                .andExpect(jsonPath("$.msg").value("系统异常，请稍候再试"))
                .andExpect(jsonPath("$.feedbackLevel").value("ERROR"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.traceId").value(org.hamcrest.Matchers.nullValue()));
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
