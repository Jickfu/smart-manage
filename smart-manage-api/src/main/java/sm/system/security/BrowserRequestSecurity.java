package sm.system.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import sm.framework.config.CorsProperties;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.util.ServletUtil;

import java.util.Set;

/** 浏览器 Cookie 认证所需的 Origin 与 CSRF 请求校验。 */
@Component
@RequiredArgsConstructor
public class BrowserRequestSecurity {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private final CorsProperties corsProperties;
    private final CsrfTokenManager csrfTokenManager;

    /** 所有非安全方法都必须来自配置的可信前端来源，包括登录前公开接口。 */
    public void validateOrigin() {
        HttpServletRequest request = ServletUtil.getRequest();
        if (SAFE_METHODS.contains(request.getMethod())) {
            return;
        }
        String origin = request.getHeader("Origin");
        if (origin == null || "null".equals(origin) || !isAllowedOrigin(origin)) {
            throw new BizException(ResultEnum.CSRF_TOKEN_INVALID);
        }
    }

    /** 已认证的非安全方法在 Origin 校验之外还必须提交会话绑定的 CSRF Token。 */
    public void validateCsrfToken() {
        HttpServletRequest request = ServletUtil.getRequest();
        if (!SAFE_METHODS.contains(request.getMethod())) {
            csrfTokenManager.validateCurrentToken(request.getHeader(CsrfTokenManager.HEADER_NAME));
        }
    }

    private boolean isAllowedOrigin(String origin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsProperties.allowedOrigins());
        return configuration.checkOrigin(origin) != null;
    }
}
