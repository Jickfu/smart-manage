package sm.domain.sys.base.login.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.BaseRedisKey;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.login.model.TemporaryLoginGrant;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.user.model.form.TemporaryLoginGrantForm;
import sm.domain.sys.base.user.model.vo.TemporaryLoginGrantVO;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.domain.sys.base.user.model.UserCacheSnapshot;
import sm.domain.sys.base.user.service.UserService;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.domain.sys.monitor.loginlog.constant.LoginEventType;
import sm.system.exception.BizException;
import sm.system.helper.SM2Helper;
import sm.system.helper.Sm2DecryptionException;
import sm.system.response.ResultEnum;
import sm.system.util.ServletUtil;
import sm.system.web.ClientIpResolver;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** 管理员一次性代登录凭证。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TemporaryLoginService {
    public static final String CREDENTIAL_PREFIX = "SMTL1.";
    private static final String SAFE_SERVICE = "temporary-login";
    private static final long SAFE_SECONDS = 5 * 60;
    private static final long GRANT_MINUTES = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final CurrentUserContext currentUserContext;
    private final UserService userService;
    private final LogWriteService logWriteService;
    private final ClientIpResolver clientIpResolver;
    private final LoginRedisAccessor loginRedisAccessor;
    private final LoginCacheJsonCodec cacheJsonCodec;

    public boolean isSafe() {
        currentUserContext.checkAdministrator();
        return StpUtil.isSafe(SAFE_SERVICE);
    }

    public void openSafe(String encryptedPassword) {
        currentUserContext.checkAdministrator();
        String password;
        try {
            password = SM2Helper.decryptJsCiphertext(encryptedPassword);
        } catch (Sm2DecryptionException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "认证数据无效，请刷新页面后重试");
        }
        if (!userService.verifyAdministratorPassword(currentUserContext.getUserId(), password)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "管理员密码错误");
        }
        StpUtil.openSafe(SAFE_SERVICE, SAFE_SECONDS);
    }

    public TemporaryLoginGrantVO createGrant(TemporaryLoginGrantForm form) {
        currentUserContext.checkAdministrator();
        StpUtil.checkSafe(SAFE_SERVICE);
        UserCacheSnapshot target = userService.requireUser(form.getUserId());
        UserAuthentication authentication = userService.authenticateTemporaryLogin(target.getId(), target.getUsername());
        if (!authentication.successful()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "目标用户当前不可登录");
        }

        String credential = createCredential();
        String grantId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(GRANT_MINUTES);
        TemporaryLoginGrant grant = new TemporaryLoginGrant(grantId, currentUserContext.getUserId(),
                target.getId(), target.getUsername(), form.getReason().trim(), expiresAt);
        redisTemplate.opsForValue().set(
                redisKey(credential), cacheJsonCodec.write(grant), GRANT_MINUTES, TimeUnit.MINUTES);
        RequestMeta requestMeta = requestMeta();
        logWriteService.writeTemporaryLoginEvent(target.getId(), target.getUsername(), target.getName(),
                grant.getIssuerUserId(), grantId, grant.getReason(), expiresAt,
                LoginEventType.TEMPORARY_LOGIN_GRANT_CREATED, requestMeta.ip(), requestMeta.userAgent());
        return new TemporaryLoginGrantVO(credential, expiresAt);
    }

    public LoginVO consume(String username, String credential) {
        TemporaryLoginGrant grant = cacheJsonCodec.read(
                loginRedisAccessor.getAndDelete(redisKey(credential)), TemporaryLoginGrant.class);
        if (grant == null || !grant.getTargetUsername().equals(username)) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "用户名或密码错误");
        }
        UserAuthentication authentication = userService.authenticateTemporaryLogin(
                grant.getTargetUserId(), grant.getTargetUsername());
        if (!authentication.successful()) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "用户名或密码错误");
        }
        LoginVO login = userService.completeTemporaryLogin(authentication, grant.getIssuerUserId(),
                grant.getGrantId(), grant.getReason());
        RequestMeta requestMeta = requestMeta();
        logWriteService.writeTemporaryLoginEvent(authentication.userId(), authentication.username(),
                authentication.name(), grant.getIssuerUserId(), grant.getGrantId(), grant.getReason(),
                grant.getExpiresAt(), LoginEventType.TEMPORARY_LOGIN_SUCCESS,
                requestMeta.ip(), requestMeta.userAgent());
        return login;
    }

    public boolean supports(String credential) {
        return credential != null && credential.startsWith(CREDENTIAL_PREFIX);
    }

    private String createCredential() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return CREDENTIAL_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String redisKey(String credential) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(credential.getBytes(StandardCharsets.UTF_8));
            return BaseRedisKey.TEMPORARY_LOGIN_GRANT + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK 缺少 SHA-256 算法", exception);
        }
    }

    private RequestMeta requestMeta() {
        String ip = null;
        String userAgent = null;
        try {
            ip = clientIpResolver.resolveCurrentRequest();
            userAgent = ServletUtil.getRequest().getHeader("User-Agent");
        } catch (Exception exception) {
            log.warn("获取代登录请求客户端信息失败: {}", exception.getMessage());
        }
        return new RequestMeta(ip, userAgent);
    }

    private record RequestMeta(String ip, String userAgent) { }
}
