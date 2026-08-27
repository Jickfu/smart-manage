package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.common.constant.BaseCacheName;
import com.alicp.jetcache.anno.CacheInvalidate;
import sm.domain.sys.base.login.constant.LoginProtectionParam;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.message.email.contract.EmailNotificationSender;
import sm.domain.sys.message.email.contract.SensitiveEmailNotificationCommand;
import sm.system.aop.log.BizLog;
import sm.system.auth.SessionTerminationReason;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;
import sm.system.security.crypto.BrowserPasswordCipher;
import sm.system.security.crypto.Sm2CiphertextException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** 邮箱验证码改密边界，公开找回和当前用户改密共享同一套一次性消费规则。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserEmailPasswordService {
    public static final String GENERIC_SEND_MESSAGE = "如果账号存在且已绑定邮箱，验证码将发送到该邮箱";
    private static final String KEY_PREFIX = "sys:base:email-password:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DefaultRedisScript<Long> VERIFY_SCRIPT = new DefaultRedisScript<>("""
            local expected = redis.call('GET', KEYS[1])
            if not expected then return -1 end
            if expected == ARGV[1] then
                redis.call('DEL', KEYS[1], KEYS[2])
                return 1
            end
            local attempts = redis.call('INCR', KEYS[2])
            if attempts == 1 then redis.call('EXPIRE', KEYS[2], ARGV[3]) end
            if attempts >= tonumber(ARGV[2]) then redis.call('DEL', KEYS[1], KEYS[2]) end
            return 0
            """, Long.class);

    private final UserMapper userMapper;
    private final UserTxService txService;
    private final EmailNotificationSender emailNotificationSender;
    private final StringRedisTemplate redisTemplate;
    private final SysParamService sysParamService;
    private final CurrentUserContext currentUserContext;
    private final BrowserPasswordCipher browserPasswordCipher;
    private final AuthorizationStateHelper authorizationStateHelper;
    private final UserAuthenticationService userAuthenticationService;

    public void requestPublicCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        UserEntity user = findEligibleByEmail(normalizedEmail);
        if (user == null) {
            reserveUnknownRequest(normalizedEmail);
            return;
        }
        try {
            issueAndSend(user, normalizedEmail, "recovery", false);
        } catch (RuntimeException exception) {
            log.warn("密码找回验证码邮件创建失败");
        }
    }

    public void resetPublicPassword(String email, String code, String encryptedNewPassword) {
        UserEntity user = findEligibleByEmail(normalizeEmail(email));
        if (user == null || !consumeCode("recovery", user.getId(), code)) {
            throw new BizException(ResultEnum.CAPTCHA_ERROR, "邮箱验证码无效或已过期");
        }
        String newPassword = decryptNewPassword(encryptedNewPassword);
        txService.updatePasswordByVerifiedEmail(user.getId(), newPassword);
        terminate(user.getId());
    }

    public void requestCurrentCode() {
        UserEntity user = requireCurrentEligibleUser();
        issueAndSend(user, user.getEmail(), "current", true);
    }

    @BizLog(value = "通过邮箱验证码修改个人密码", recordResponse = false)
    public void changeCurrentPassword(String code, String encryptedNewPassword) {
        UserEntity user = requireCurrentEligibleUser();
        if (!consumeCode("current", user.getId(), code)) {
            throw new BizException(ResultEnum.CAPTCHA_ERROR, "邮箱验证码无效或已过期");
        }
        String newPassword = decryptNewPassword(encryptedNewPassword);
        txService.updatePasswordByVerifiedEmail(user.getId(), newPassword);
        terminate(user.getId());
    }

    public void requestEmailBinding(String encryptedPassword, String email) {
        Long userId = currentUserContext.getUserId();
        String password;
        try {
            password = browserPasswordCipher.decrypt(encryptedPassword);
        } catch (Sm2CiphertextException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码加密数据无效");
        }
        if (!userAuthenticationService.verifyCurrentPassword(userId, password)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码不正确");
        }
        String normalizedEmail = normalizeEmail(email);
        UserEntity occupied = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)
                .ne(UserEntity::getId, userId));
        if (occupied != null) throw new BizException(ResultEnum.UNIQUE_CONFLICT, "邮箱已被其他账号使用");
        UserEntity user = userMapper.selectById(userId);
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
        issueBindingCode(user, normalizedEmail);
    }

    @BizLog(value = "验证并绑定个人邮箱", recordResponse = false)
    @CacheInvalidate(name = BaseCacheName.USER_INFO, key = "@currentUserContext.getUserId()")
    public void bindCurrentEmail(String email, String code) {
        Long userId = currentUserContext.getUserId();
        String normalizedEmail = normalizeEmail(email);
        if (!consumeCode("bind:" + digest(normalizedEmail), userId, code)) {
            throw new BizException(ResultEnum.CAPTCHA_ERROR, "邮箱验证码无效或已过期");
        }
        txService.bindVerifiedEmail(userId, normalizedEmail);
    }

    private void issueAndSend(UserEntity user, String email, String purpose, boolean exposeFailure) {
        int resendSeconds = positive(LoginProtectionParam.PASSWORD_EMAIL_CODE_RESEND_SECONDS);
        String resendKey = resendKey(purpose, user.getId());
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                resendKey, "1", resendSeconds, TimeUnit.SECONDS))) {
            throw new BizException(ResultEnum.REQUEST_LIMIT, "邮箱验证码发送过于频繁");
        }
        String code = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        int expireMinutes = positive(LoginProtectionParam.PASSWORD_EMAIL_CODE_EXPIRE_MINUTES);
        String codeKey = codeKey(purpose, user.getId());
        redisTemplate.opsForValue().set(codeKey, digest(code), expireMinutes, TimeUnit.MINUTES);
        redisTemplate.delete(attemptKey(purpose, user.getId()));
        String subject = "Smart Manage 密码验证码";
        String text = "你的验证码是 " + code + "，" + expireMinutes + " 分钟内有效。请勿向任何人泄露。";
        String html = "<p>你的验证码是：<strong>" + code + "</strong></p><p>验证码 "
                + expireMinutes + " 分钟内有效，请勿向任何人泄露。</p>";
        try {
            emailNotificationSender.enqueueSensitive(new SensitiveEmailNotificationCommand(
                    "security.password-code", "password-code:" + UUID.randomUUID(),
                    List.of(email), subject, html, text));
        } catch (RuntimeException exception) {
            redisTemplate.delete(List.of(codeKey, resendKey, attemptKey(purpose, user.getId())));
            if (exposeFailure) throw exception;
            throw exception;
        }
    }

    private void issueBindingCode(UserEntity user, String email) {
        String purpose = "bind:" + digest(email);
        issueAndSend(user, email, purpose, true);
    }

    private boolean consumeCode(String purpose, Long userId, String code) {
        int maxAttempts = positive(LoginProtectionParam.PASSWORD_EMAIL_CODE_MAX_ATTEMPTS);
        int expireSeconds = positive(LoginProtectionParam.PASSWORD_EMAIL_CODE_EXPIRE_MINUTES) * 60;
        Long result = redisTemplate.execute(VERIFY_SCRIPT,
                List.of(codeKey(purpose, userId), attemptKey(purpose, userId)),
                digest(code), String.valueOf(maxAttempts), String.valueOf(expireSeconds));
        return Long.valueOf(1L).equals(result);
    }

    private UserEntity findEligibleByEmail(String email) {
        if (!StringUtils.hasText(email)) return null;
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email));
        if (user == null || user.getEmailVerifiedAt() == null || !Boolean.TRUE.equals(user.getEnabled())
                || UserConstant.SUPER_ADMIN.equals(user.getUsername())) return null;
        return user;
    }

    private UserEntity requireCurrentEligibleUser() {
        UserEntity user = userMapper.selectById(currentUserContext.getUserId());
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
        if (UserConstant.SUPER_ADMIN.equals(user.getUsername())) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "超级管理员不允许通过邮箱修改密码");
        }
        if (!StringUtils.hasText(user.getEmail()) || user.getEmailVerifiedAt() == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "当前账号尚未绑定已验证邮箱");
        }
        return user;
    }

    private String decryptNewPassword(String encryptedPassword) {
        try {
            String password = browserPasswordCipher.decrypt(encryptedPassword);
            if (password.length() < 8) throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能少于8位");
            return password;
        } catch (Sm2CiphertextException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码加密数据无效");
        }
    }

    private void terminate(Long userId) {
        authorizationStateHelper.terminateUsers(List.of(userId), SessionTerminationReason.PASSWORD_RESET_TERMINATED);
    }

    private void reserveUnknownRequest(String email) {
        int resendSeconds = positive(LoginProtectionParam.PASSWORD_EMAIL_CODE_RESEND_SECONDS);
        redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + "unknown:" + digest(email), "1",
                resendSeconds, TimeUnit.SECONDS);
    }

    private int positive(String number) {
        Integer value = sysParamService.getInt(number);
        if (value == null || value <= 0) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "邮箱验证码参数 " + number + " 必须为正整数");
        }
        return value;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境缺少 SHA-256", exception);
        }
    }

    private static String codeKey(String purpose, Long userId) { return KEY_PREFIX + purpose + ":code:" + userId; }
    private static String attemptKey(String purpose, Long userId) { return KEY_PREFIX + purpose + ":attempt:" + userId; }
    private static String resendKey(String purpose, Long userId) { return KEY_PREFIX + purpose + ":resend:" + userId; }
}
