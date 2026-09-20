package sm.domain.sys.base.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sm.domain.sys.base.login.constant.LoginProtectionParam;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 登录防滥用状态协调器。
 *
 * <p>所有状态保存在共享 Redis。账号和 IP 仅以摘要进入 Key，避免缓存管理和诊断输出直接暴露身份。</p>
 */
@Component
@RequiredArgsConstructor
class LoginProtectionService {
    private static final String KEY_PREFIX = "sys:base:login-protection:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final SysParamService sysParamService;
    private final LoginRedisAccessor loginRedisAccessor;

    public void prepareChallenge(String username, String clientIp) {
        IdentityKeys identity = identity(username, clientIp);
        assertNotBlocked(identity);

        int intervalMillis = requiredPositiveInt(LoginProtectionParam.CAPTCHA_MIN_INTERVAL_MILLIS);
        Boolean intervalAccepted = redisTemplate.opsForValue().setIfAbsent(
                KEY_PREFIX + "captcha-interval:" + identity.ipDigest(), "1", intervalMillis, TimeUnit.MILLISECONDS);
        if (!Boolean.TRUE.equals(intervalAccepted)) {
            throw new BizException(ResultEnum.REQUEST_LIMIT, "验证码获取过于频繁");
        }

        int maxPerMinute = requiredPositiveInt(LoginProtectionParam.CAPTCHA_IP_MAX_PER_MINUTE);
        long count = incrementWithWindow(
                KEY_PREFIX + "captcha-minute:" + identity.ipDigest(), 1, TimeUnit.MINUTES);
        if (count > maxPerMinute) {
            throw new BizException(ResultEnum.REQUEST_LIMIT, "验证码获取过于频繁");
        }
    }

    public String issueCaptchaTicket(String username) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String ticket = HexFormat.of().formatHex(randomBytes);
        int expireSeconds = requiredPositiveInt(LoginProtectionParam.CAPTCHA_TICKET_EXPIRE_SECONDS);
        redisTemplate.opsForValue().set(ticketKey(ticket), accountDigest(username), expireSeconds, TimeUnit.SECONDS);
        return ticket;
    }

    public void consumeCaptchaTicket(String username, String clientIp, String ticket) {
        IdentityKeys identity = identity(username, clientIp);
        assertNotBlocked(identity);
        String boundAccount = loginRedisAccessor.getAndDelete(ticketKey(ticket));
        if (boundAccount == null) {
            throw new BizException(ResultEnum.CAPTCHA_EXPIRE, "滑块验证已失效，请重新验证");
        }
        if (!identity.accountDigest().equals(boundAccount)) {
            throw new BizException(ResultEnum.CAPTCHA_ERROR, "滑块验证与当前账号不匹配");
        }
    }

    public void recordAuthenticationFailure(String username, String clientIp) {
        IdentityKeys identity = identity(username, clientIp);
        int windowMinutes = requiredPositiveInt(LoginProtectionParam.FAILURE_WINDOW_MINUTES);

        long accountFailures = incrementWithWindow(accountCounterKey(identity), windowMinutes, TimeUnit.MINUTES);
        if (accountFailures >= requiredPositiveInt(LoginProtectionParam.ACCOUNT_MAX_FAILURES)) {
            int blockSeconds = requiredPositiveInt(LoginProtectionParam.ACCOUNT_BLOCK_SECONDS);
            redisTemplate.opsForValue().set(accountBlockKey(identity), "1", blockSeconds, TimeUnit.SECONDS);
        }

        long ipFailures = incrementWithWindow(ipCounterKey(identity), windowMinutes, TimeUnit.MINUTES);
        if (ipFailures >= requiredPositiveInt(LoginProtectionParam.IP_MAX_FAILURES)) {
            int blockMinutes = requiredPositiveInt(LoginProtectionParam.IP_BLOCK_MINUTES);
            redisTemplate.opsForValue().set(ipBlockKey(identity), "1", blockMinutes, TimeUnit.MINUTES);
        }

        long pairFailures = incrementWithWindow(pairCounterKey(identity), windowMinutes, TimeUnit.MINUTES);
        if (pairFailures >= requiredPositiveInt(LoginProtectionParam.ACCOUNT_IP_MAX_FAILURES)) {
            int blockMinutes = requiredPositiveInt(LoginProtectionParam.ACCOUNT_IP_BLOCK_MINUTES);
            redisTemplate.opsForValue().set(pairBlockKey(identity), "1", blockMinutes, TimeUnit.MINUTES);
        }
    }

    public void clearAfterSuccess(String username, String clientIp) {
        IdentityKeys identity = identity(username, clientIp);
        redisTemplate.delete(List.of(
                accountCounterKey(identity),
                accountBlockKey(identity),
                pairCounterKey(identity),
                pairBlockKey(identity)));
        // IP 维度反映来源整体风险，登录成功后仍按窗口自然过期。
    }

    private void assertNotBlocked(IdentityKeys identity) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(pairBlockKey(identity)))) {
            throw new BizException(ResultEnum.REQUEST_LIMIT, "当前账号和网络登录失败次数过多");
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(ipBlockKey(identity)))) {
            throw new BizException(ResultEnum.REQUEST_LIMIT, "当前网络登录请求过多");
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(accountBlockKey(identity)))) {
            throw new BizException(ResultEnum.REQUEST_LIMIT, "当前账号登录失败次数过多");
        }
    }

    private long incrementWithWindow(String key, int duration, TimeUnit timeUnit) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            throw new BizException(ResultEnum.SERVER_ERROR, "认证服务暂不可用");
        }
        if (count == 1L) {
            redisTemplate.expire(key, duration, timeUnit);
        }
        return count;
    }

    private IdentityKeys identity(String username, String clientIp) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(clientIp)) {
            throw new BizException(ResultEnum.SERVER_ERROR, "无法建立登录保护上下文");
        }
        return new IdentityKeys(accountDigest(username), digest(clientIp.trim()));
    }

    private String accountDigest(String username) {
        return digest(username.trim().toLowerCase(Locale.ROOT));
    }

    private String digest(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境缺少 SHA-256", exception);
        }
    }

    private int requiredPositiveInt(String number) {
        Integer value = sysParamService.getInt(number);
        if (value == null || value <= 0) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "登录保护参数 " + number + " 必须为正整数");
        }
        return value;
    }

    private String ticketKey(String ticket) {
        return KEY_PREFIX + "captcha-ticket:" + ticket;
    }

    private String accountCounterKey(IdentityKeys identity) {
        return KEY_PREFIX + "account-failures:" + identity.accountDigest();
    }

    private String accountBlockKey(IdentityKeys identity) {
        return KEY_PREFIX + "account-block:" + identity.accountDigest();
    }

    private String ipCounterKey(IdentityKeys identity) {
        return KEY_PREFIX + "ip-failures:" + identity.ipDigest();
    }

    private String ipBlockKey(IdentityKeys identity) {
        return KEY_PREFIX + "ip-block:" + identity.ipDigest();
    }

    private String pairCounterKey(IdentityKeys identity) {
        return KEY_PREFIX + "pair-failures:" + identity.accountDigest() + ":" + identity.ipDigest();
    }

    private String pairBlockKey(IdentityKeys identity) {
        return KEY_PREFIX + "pair-block:" + identity.accountDigest() + ":" + identity.ipDigest();
    }

    private record IdentityKeys(String accountDigest, String ipDigest) {
    }
}
