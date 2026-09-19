package sm.domain.sys.base.login.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.system.security.CsrfTokenManager;
import sm.system.security.SessionCredentialGuard;
import sm.system.security.context.CurrentUserContext;

/** 登录会话创建及身份上下文初始化服务。 */
@Service
@RequiredArgsConstructor
class UserSessionService {
    private final CurrentUserContext currentUserContext;
    private final CsrfTokenManager csrfTokenManager;

    public LoginVO completeLogin(UserAuthentication authentication) {
        requireGeneration(authentication);
        StpUtil.login(authentication.userId());
        currentUserContext.initializeIdentity(
                authentication.orgId(), authentication.username(), authentication.administrator());
        StpUtil.getTokenSession().set(SessionCredentialGuard.GENERATION_CLAIM, authentication.credentialGeneration().toString());
        csrfTokenManager.initializeCurrentSession();
        LoginVO result = new LoginVO();
        result.setAuthenticated(true);
        return result;
    }

    public LoginVO completeTemporaryLogin(UserAuthentication authentication, Long issuerUserId,
            String grantId, String reason) {
        requireGeneration(authentication);
        SaLoginParameter parameter = new SaLoginParameter()
                .setTimeout(30 * 60)
                .setDevice("temporary-admin-login")
                .setIsLastingCookie(false);
        StpUtil.login(authentication.userId(), parameter);
        currentUserContext.initializeIdentity(authentication.orgId(), authentication.username(), false);
        StpUtil.getTokenSession().set(SessionCredentialGuard.GENERATION_CLAIM, authentication.credentialGeneration().toString());
        csrfTokenManager.initializeCurrentSession();
        var tokenSession = StpUtil.getTokenSession();
        tokenSession.set("authenticationMethod", "TEMPORARY_ADMIN_GRANT");
        tokenSession.set("issuerUserId", issuerUserId);
        tokenSession.set("grantId", grantId);
        tokenSession.set("temporaryLoginReason", reason);
        LoginVO result = new LoginVO();
        result.setAuthenticated(true);
        return result;
    }

    private void requireGeneration(UserAuthentication authentication) {
        if (authentication.credentialGeneration() == null || authentication.credentialGeneration() < 0) {
            throw new IllegalStateException("认证结果缺少有效凭据代际");
        }
    }
}
