package sm.domain.sys.monitor.common.listener;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sm.domain.sys.base.user.model.UserCacheSnapshot;
import sm.domain.sys.base.user.service.UserService;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.domain.sys.monitor.loginlog.constant.LoginEventType;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;
import sm.system.web.ClientIpResolver;
import sm.system.auth.SessionTerminationContext;
import sm.system.auth.SessionTerminationReason;

/**
 * Sa-Token 监听器 — 记录登录/登出日志
 *
 * @author Chekfu
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthListener implements SaTokenListener {
    private final LogWriteService logWriteService;
    private final UserService userService;
    private final ClientIpResolver clientIpResolver;

    /**
     * 登录
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter saLoginParameter) {
        try {
            LoginLogEntity e = new LoginLogEntity();
            if (loginId != null) {
                try {
                    long uid = Long.parseLong(String.valueOf(loginId));
                    e.setUserId(uid);
                    UserCacheSnapshot u = userService.requireUser(uid);
                    if (u != null) {
                        e.setUsername(u.getUsername());
                        e.setNickname(u.getName());
                    }
                } catch (Exception ignored) {
                }
            }
            e.setEventType(LoginEventType.LOGIN_SUCCESS.name());
            e.setSuccess(true);
            fillRequestMeta(e);
            logWriteService.writeLogin(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 登出
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        try {
            LoginLogEntity e = new LoginLogEntity();
            if (loginId != null) {
                try {
                    long uid = Long.parseLong(String.valueOf(loginId));
                    e.setUserId(uid);
                    UserCacheSnapshot u = userService.requireUser(uid);
                    if (u != null) {
                        e.setUsername(u.getUsername());
                        e.setNickname(u.getName());
                    }
                } catch (Exception ignored) {
                }
            }
            e.setEventType(resolveLogoutEvent().name());
            e.setSuccess(true);
            fillRequestMeta(e);
            logWriteService.writeLogin(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private LoginEventType resolveLogoutEvent() {
        SessionTerminationReason reason = SessionTerminationContext.current();
        return reason == null ? LoginEventType.LOGOUT : LoginEventType.valueOf(reason.name());
    }

    /**
     * 被踢下线
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        writeSessionEvent(loginId, LoginEventType.SESSION_KICKED);
    }

    /**
     * 被顶替下线
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        writeSessionEvent(loginId, LoginEventType.SESSION_REPLACED);
    }

    /**
     * 封禁账号
     */
    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
        writeSessionEvent(loginId, LoginEventType.ACCOUNT_DISABLED);
    }

    /**
     * 解封账号
     */
    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
    }

    /**
     * 打开二级认证
     */
    @Override
    public void doOpenSafe(String loginType, String tokenValue, String service, long l) {
    }

    /**
     * 关闭二级认证
     */
    @Override
    public void doCloseSafe(String loginType, String tokenValue, String service) {
    }

    /**
     * 创建Session
     */
    @Override
    public void doCreateSession(String id) {
    }

    /**
     * 注销Session
     */
    @Override
    public void doLogoutSession(String id) {
    }

    /**
     * Token续期
     */
    @Override
    public void doRenewTimeout(String tokenValue, Object loginId, String s1, long timeout) {
    }

    private void fillRequestMeta(LoginLogEntity e) {
        try {
            e.setIp(clientIpResolver.resolveCurrentRequest());
        } catch (Exception ignored) {
        }
        try {
            ServletRequestAttributes a = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = a != null ? a.getRequest() : null;
            if (req != null) {
                String ua = req.getHeader("User-Agent");
                if (StringUtils.hasText(ua)) {
                    e.setUserAgent(ua.length() > 1024 ? ua.substring(0, 1024) : ua);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void writeSessionEvent(Object loginId, LoginEventType eventType) {
        try {
            LoginLogEntity entity = new LoginLogEntity();
            fillIdentity(entity, loginId);
            entity.setEventType(eventType.name());
            entity.setSuccess(true);
            fillRequestMeta(entity);
            logWriteService.writeLogin(entity);
        } catch (Exception exception) {
            log.error("记录认证事件失败: {}", eventType, exception);
        }
    }

    private void fillIdentity(LoginLogEntity entity, Object loginId) {
        if (loginId == null) {
            return;
        }
        long userId = Long.parseLong(String.valueOf(loginId));
        entity.setUserId(userId);
        UserCacheSnapshot user = userService.requireUser(userId);
        entity.setUsername(user.getUsername());
        entity.setNickname(user.getName());
    }

}
