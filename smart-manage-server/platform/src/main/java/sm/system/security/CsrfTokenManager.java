package sm.system.security;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** 管理与当前 Sa-Token 会话绑定的 CSRF Token。 */
@Component
public class CsrfTokenManager {
    public static final String HEADER_NAME = "sm-csrf-token";
    public static final String SESSION_KEY = "sm-csrf-token";
    private static final int TOKEN_BYTE_LENGTH = 16;
    private static final int TOKEN_TEXT_LENGTH = TOKEN_BYTE_LENGTH * 2;
    private final SecureRandom secureRandom = new SecureRandom();

    /** 每次创建登录会话时生成独立 Token，禁止跨登录复用。 */
    public String initializeCurrentSession() {
        return initializeSession(StpUtil.getTokenValue());
    }

    /** 为显式创建的登录令牌初始化 CSRF Token。 */
    public String initializeSession(String token) {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String csrfToken = HexFormat.of().formatHex(randomBytes);
        StpUtil.getStpLogic().getTokenSessionByToken(token).set(SESSION_KEY, csrfToken);
        return csrfToken;
    }

    public String getCurrentToken() {
        String csrfToken = StpUtil.getTokenSession().getString(SESSION_KEY);
        if (csrfToken == null || csrfToken.length() != TOKEN_TEXT_LENGTH) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "当前登录会话缺少安全校验信息，请重新登录");
        }
        return csrfToken;
    }

    /** 使用恒定时间比较，避免通过比较耗时推断 Token 内容。 */
    public void validateCurrentToken(String submittedToken) {
        if (submittedToken == null || submittedToken.length() != TOKEN_TEXT_LENGTH) {
            throw csrfFailure();
        }
        byte[] expected = getCurrentToken().getBytes(StandardCharsets.US_ASCII);
        byte[] submitted = submittedToken.getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(expected, submitted)) {
            throw csrfFailure();
        }
    }

    private BizException csrfFailure() {
        return new BizException(ResultEnum.CSRF_TOKEN_INVALID);
    }
}
