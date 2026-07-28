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
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.service.UserService;
import sm.domain.sys.monitor.common.service.LogWriteService;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;
import sm.system.util.ServletUtil;

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
                    UserEntity u = userService.requireUser(uid);
                    if (u != null) {
                        e.setUsername(u.getUsername());
                        e.setNickname(u.getNickname());
                    }
                } catch (Exception ignored) {
                }
            }
            e.setEventType("LOGIN");
            e.setSuccess(true);
            e.setTokenHint(tokenHint(tokenValue));
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
                    UserEntity u = userService.requireUser(uid);
                    if (u != null) {
                        e.setUsername(u.getUsername());
                        e.setNickname(u.getNickname());
                    }
                } catch (Exception ignored) {
                }
            }
            e.setEventType("LOGOUT");
            e.setSuccess(true);
            e.setTokenHint(tokenHint(tokenValue));
            fillRequestMeta(e);
            logWriteService.writeLogin(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 被踢下线
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
    }

    /**
     * 被顶替下线
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
    }

    /**
     * 封禁账号
     */
    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
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
            e.setIp(ServletUtil.getClientIp());
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

    private String tokenHint(String tokenValue) {
        if (!StringUtils.hasText(tokenValue)) {
            return null;
        }
        if (tokenValue.length() <= 8) {
            return "***";
        }
        return tokenValue.substring(0, 4) + "..." + tokenValue.substring(tokenValue.length() - 4);
    }
}
