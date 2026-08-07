package sm.domain.sys.base.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseKeyConstantsTests {

    @Test
    void keyNamesMustKeepSystemBaseApplicationPrefix() {
        assertEquals("sys:base:", BaseKeyPrefix.VALUE);
        assertEquals("sys:base:user-info", BaseCacheName.USER_INFO);
        assertEquals("sys:base:sys-param", BaseCacheName.SYS_PARAM);
        assertEquals("sys:base:basic-data-options", BaseCacheName.BASIC_DATA_OPTIONS);
        assertEquals("sys:base:captcha:", BaseRedisKey.CAPTCHA);
        assertEquals("sys:base:password-change:", BaseRedisKey.PASSWORD_CHANGE_TICKET);
    }
}
