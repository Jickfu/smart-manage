package sm.system.security;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 在进入业务前校验登录时的凭据代际，旧令牌即使仍在 Redis 中也没有访问权限。 */
@Component
@RequiredArgsConstructor
public class SessionCredentialGuard {
    public static final String GENERATION_CLAIM = "credentialGeneration";
    private final AuthenticatedSessionStateVerifier verifier;

    public void checkCurrent() {
        var session = StpUtil.getStpLogic().getTokenSession(false);
        Object claim = session == null ? null : session.get(GENERATION_CLAIM);
        // 旧版本会话缺少声明必须失效，禁止将缺失值默认成初始代际。
        if (!(claim instanceof String value) || !value.matches("0|[1-9][0-9]{0,18}")) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
        long generation;
        try {
            generation = Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
        verifier.verify(StpUtil.getLoginIdAsLong(), generation);
    }
}
