package sm.system.json.masking;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class MaskedValueSerializerTests {
    private static final String PERMISSION = "sys:base:user:sensitive:read";

    @Test
    void masksByDefaultWhenSecurityContextIsUnavailable() throws Exception {
        Contact contact = new Contact("13812341234");
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);

            assertEquals("{\"phone\":\"138****1234\"}", JsonMapper.builder().build().writeValueAsString(contact));
        }
    }

    @Test
    void revealsOnlyWhenExplicitPermissionIsGranted() throws Exception {
        Contact contact = new Contact("13812341234");
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(() -> StpUtil.hasPermission(PERMISSION)).thenReturn(true);

            assertEquals("{\"phone\":\"13812341234\"}", JsonMapper.builder().build().writeValueAsString(contact));
        }
    }

    private static final class Contact {
        @Masked(type = MaskingType.PHONE, revealPermission = PERMISSION)
        private final String phone;

        private Contact(String phone) {
            this.phone = phone;
        }

        public String getPhone() {
            return phone;
        }
    }
}
