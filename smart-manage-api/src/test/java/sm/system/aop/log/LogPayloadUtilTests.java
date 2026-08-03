package sm.system.aop.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LogPayloadUtilTests {

    @Test
    void sensitiveJsonFieldsAreMasked() {
        String payload = """
                {"username":"administrator","password":"secret","newPassword":"changed",\
"passwordChangeTicket":"ticket-value","privateKey":"private-value","token":"abc"}""";

        String masked = LogPayloadUtil.maskJsonLike(payload);

        assertFalse(masked.contains("secret"));
        assertFalse(masked.contains("changed"));
        assertFalse(masked.contains("ticket-value"));
        assertFalse(masked.contains("private-value"));
        assertFalse(masked.contains("\"abc\""));
        assertEquals("""
                {"username":"administrator","password":"***","newPassword":"***",\
"passwordChangeTicket":"***","privateKey":"***","token":"***"}""", masked);
    }

    @Test
    void longPayloadIsExplicitlyTruncated() {
        assertEquals("1234...(truncated)", LogPayloadUtil.truncate("123456", 4));
    }
}
