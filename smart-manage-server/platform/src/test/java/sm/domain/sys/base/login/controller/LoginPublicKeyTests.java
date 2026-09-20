package sm.domain.sys.base.login.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import sm.domain.sys.base.login.service.LoginService;
import sm.system.security.SessionCredentialGuard;
import sm.system.security.config.SaTokenConfig;
import sm.system.security.web.BrowserRequestSecurity;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginPublicKeyTests {
    @Test
    void anonymousRequestReturnsOnlyPublicKeyAndDisablesCaching() throws Exception {
        LoginService service = mock(LoginService.class);
        when(service.passwordPublicKey()).thenReturn("deployment-public-key");
        LoginController controller = new LoginController(service);
        var initialMvc = MockMvcBuilders.standaloneSetup(controller).build();
        var mapping = initialMvc.getDispatcherServlet().getWebApplicationContext()
                .getBean(RequestMappingHandlerMapping.class);
        var config = new SaTokenConfig(mapping, JsonMapper.builder().build(),
                mock(BrowserRequestSecurity.class), mock(SessionCredentialGuard.class));
        ReflectionTestUtils.setField(config, "noNeedLogin", new String[0]);
        // 使用真实注解发现和安全过滤器，不把新接口写进测试免登录白名单。
        new cn.dev33.satoken.spring.SaTokenContextRegister();
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new cn.dev33.satoken.filter.SaTokenContextFilterForJakartaServlet(),
                        config.getSaServletFilter()).build();
        mvc.perform(get("/sys/base/login/password/publicKey"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("deployment-public-key"));
        verify(service).passwordPublicKey();
        verifyNoMoreInteractions(service);
    }
}
