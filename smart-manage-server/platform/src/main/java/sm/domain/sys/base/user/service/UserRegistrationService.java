package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.sysparam.model.RegistrationPolicy;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.crypto.BrowserPasswordCipher;
import sm.system.security.crypto.Sm2CiphertextException;

import java.util.List;
import java.util.UUID;

/** 自助注册命令只接受邮箱凭据，归属、角色及启用状态由服务端决定。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {
    public static final String SEND_MESSAGE = "如果该邮箱可以注册，验证码将发送到该邮箱";
    private static final String LIMIT_KEY = "sys:base:registration:{send}:";
    private static final DefaultRedisScript<Long> RESERVE = new DefaultRedisScript<>("""
            for index, key in ipairs(KEYS) do
                if tonumber(redis.call('GET', key) or '0') >= tonumber(ARGV[index]) then return 0 end
            end
            for _, key in ipairs(KEYS) do
                if redis.call('INCR', key) == 1 then redis.call('EXPIRE', key, 3600) end
            end
            return 1
            """, Long.class);

    private final SysParamService sysParamService;
    private final OrgReferenceReader orgReferenceReader;
    private final UserMapper userMapper;
    private final UserEmailPasswordService emailCodes;
    private final UserRegistrationTxService txService;
    private final StringRedisTemplate redisTemplate;
    private final BrowserPasswordCipher browserPasswordCipher;
    private final PasswordPolicyService passwordPolicyService;

    public boolean available() {
        RegistrationPolicy policy = sysParamService.getRegistrationPolicy();
        if (!policy.enabled()) return false;
        if (policy.orgId() == null) return true;
        var organization = orgReferenceReader.findByIds(List.of(policy.orgId())).get(policy.orgId());
        return organization != null && organization.enabled() && !organization.archived();
    }

    public void requestCode(String email, String clientIp) {
        requireAvailable();
        String normalizedEmail = UserEmailPasswordService.normalizeEmail(email);
        // 占用与未占用邮箱都消耗同一发送额度；响应不泄露账号状态。
        Long reserved = redisTemplate.execute(RESERVE, List.of(
                LIMIT_KEY + "ip:" + UserEmailPasswordService.digest(clientIp),
                LIMIT_KEY + "email:" + UserEmailPasswordService.digest(normalizedEmail),
                LIMIT_KEY + "global"), "20", "5", "300");
        if (!Long.valueOf(1).equals(reserved)) {
            throw new BizException(ResultEnum.REQUEST_LIMIT, "注册验证码发送过于频繁，请稍后再试");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)) > 0) return;
        try {
            emailCodes.issueRegistrationCode(normalizedEmail);
        } catch (RuntimeException exception) {
            log.warn("注册验证码邮件未创建，原因类型 {}", exception.getClass().getSimpleName());
        }
    }

    public void register(String email, String code, String encryptedPassword) {
        requireAvailable();
        String normalizedEmail = UserEmailPasswordService.normalizeEmail(email);
        String password;
        try {
            password = browserPasswordCipher.decrypt(encryptedPassword);
        } catch (Sm2CiphertextException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码加密数据无效");
        }
        String username = "u_" + UUID.randomUUID().toString().replace("-", "");
        passwordPolicyService.validate(password, username);
        if (!emailCodes.consumeRegistrationCode(normalizedEmail, code)) {
            throw new BizException(ResultEnum.CAPTCHA_ERROR, "邮箱验证码无效或已过期");
        }
        try {
            txService.register(username, normalizedEmail, password);
        } catch (DuplicateKeyException exception) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "无法注册，请重新申请验证码或登录已有账号");
        }
    }

    private void requireAvailable() {
        if (!available()) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "邮箱注册暂不可用");
        }
    }
}
