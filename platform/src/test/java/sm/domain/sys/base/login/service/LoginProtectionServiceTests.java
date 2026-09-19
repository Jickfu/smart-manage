package sm.domain.sys.base.login.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import sm.domain.sys.base.login.constant.LoginProtectionParam;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginProtectionServiceTests {
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final SysParamService sysParamService = mock(SysParamService.class);
    private final LoginRedisAccessor loginRedisAccessor = mock(LoginRedisAccessor.class);
    private LoginProtectionService service;

    @BeforeEach
    void setUp() {
        service = new LoginProtectionService(redisTemplate, sysParamService, loginRedisAccessor);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
    }

    @Test
    void captchaCreationAppliesIntervalAndMinuteLimit() {
        when(sysParamService.getInt(LoginProtectionParam.CAPTCHA_MIN_INTERVAL_MILLIS)).thenReturn(1000);
        when(sysParamService.getInt(LoginProtectionParam.CAPTCHA_IP_MAX_PER_MINUTE)).thenReturn(10);
        when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(1000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        service.prepareChallenge("Administrator", "127.0.0.1");

        verify(redisTemplate).expire(anyString(), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    void captchaCreationRejectsRequestsInsideMinimumInterval() {
        when(sysParamService.getInt(LoginProtectionParam.CAPTCHA_MIN_INTERVAL_MILLIS)).thenReturn(1000);
        when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(1000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(false);

        BizException exception = assertThrows(BizException.class,
                () -> service.prepareChallenge("administrator", "127.0.0.1"));

        assertEquals(ResultEnum.REQUEST_LIMIT.getCode(), exception.getCode());
    }

    @Test
    void captchaTicketIsBoundToNormalizedAccountAndConsumedAtomically() throws Exception {
        when(sysParamService.getInt(LoginProtectionParam.CAPTCHA_TICKET_EXPIRE_SECONDS)).thenReturn(90);
        String ticket = service.issueCaptchaTicket("Administrator");
        String accountDigest = digest("administrator");
        when(loginRedisAccessor.getAndDelete("sys:base:login-protection:captcha-ticket:" + ticket))
                .thenReturn(accountDigest);

        service.consumeCaptchaTicket("administrator", "127.0.0.1", ticket);

        verify(loginRedisAccessor).getAndDelete("sys:base:login-protection:captcha-ticket:" + ticket);
    }

    @Test
    void expiredCaptchaTicketCannotReachPasswordAuthentication() {
        when(loginRedisAccessor.getAndDelete(anyString())).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
                () -> service.consumeCaptchaTicket("administrator", "127.0.0.1", "expired"));

        assertEquals(ResultEnum.CAPTCHA_EXPIRE.getCode(), exception.getCode());
    }

    @Test
    void blockedAccountIpPairIsRejectedBeforeCreatingChallenge() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        BizException exception = assertThrows(BizException.class,
                () -> service.prepareChallenge("administrator", "127.0.0.1"));

        assertEquals(ResultEnum.REQUEST_LIMIT.getCode(), exception.getCode());
    }

    private String digest(String value) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
