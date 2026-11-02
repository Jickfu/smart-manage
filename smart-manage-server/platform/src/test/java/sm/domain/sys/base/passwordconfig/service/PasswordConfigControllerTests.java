package sm.domain.sys.base.passwordconfig.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sm.domain.sys.base.passwordconfig.controller.PasswordConfigController;
import sm.domain.sys.base.passwordconfig.model.vo.PasswordPolicyVO;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 公开规则只读且不缓存；非法配置必须在进入保存服务前拒绝。 */
class PasswordConfigControllerTests {
    @Test
    void publicSnapshotExcludesManagementIdentityAndDisallowsCaching() throws Exception {
        var service = mock(PasswordConfigService.class);
        when(service.policy()).thenReturn(new PasswordPolicyVO(8, 64, true));
        var mvc = MockMvcBuilders.standaloneSetup(new PasswordConfigController(service)).build();
        mvc.perform(get("/sys/base/login/password/policy"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.minLength").value(8))
                .andExpect(jsonPath("$.data.maxLength").value(64))
                .andExpect(jsonPath("$.data.complexityEnabled").value(true))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.data.version").doesNotExist());
    }

    @Test
    void validIntegerLengthReachesSaveWithoutChangingItsValue() throws Exception {
        var service = mock(PasswordConfigService.class);
        when(service.save(any())).thenReturn(1L);
        var mvc = MockMvcBuilders.standaloneSetup(new PasswordConfigController(service)).build();
        mvc.perform(post("/sys/base/password-config/save").contentType("application/json")
                .content("{\"id\":1,\"version\":0,\"minLength\":8,\"complexityEnabled\":true}"))
                .andExpect(status().isOk());
        verify(service).save(argThat(form -> form.getMinLength() == 8 && form.getComplexityEnabled()));
    }

    @Test
    void invalidLengthAndMissingSwitchCannotReachSave() throws Exception {
        var service = mock(PasswordConfigService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new PasswordConfigController(service)).build();
        for (String body : new String[]{
                "{\"id\":1,\"version\":0,\"minLength\":3,\"complexityEnabled\":true}",
                "{\"id\":1,\"version\":0,\"minLength\":65,\"complexityEnabled\":false}",
                "{\"id\":1,\"version\":0,\"minLength\":8.9,\"complexityEnabled\":true}",
                "{\"id\":1,\"version\":0,\"minLength\":8.0,\"complexityEnabled\":true}",
                "{\"id\":1,\"version\":0,\"minLength\":\"8\",\"complexityEnabled\":true}",
                "{\"id\":1,\"version\":0,\"minLength\":8}"}) {
            mvc.perform(post("/sys/base/password-config/save").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
}
