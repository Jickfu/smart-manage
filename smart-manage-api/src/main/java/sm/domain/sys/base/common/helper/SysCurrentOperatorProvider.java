package sm.domain.sys.base.common.helper;

import cn.dev33.satoken.exception.SaTokenContextException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.system.helper.CurrentOperatorProvider;

/** 基于当前 Sa-Token 会话提供审计操作人。 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SysCurrentOperatorProvider implements CurrentOperatorProvider {
	private final CurrentUserContext currentUserContext;

    @Override
    public Long getCurrentUserIdOrNull() {
        try {
            return currentUserContext.isLogin() ? currentUserContext.getUserId() : null;
        } catch (SaTokenContextException exception) {
            return null;
        } catch (Exception exception) {
            log.warn("读取当前审计用户失败", exception);
            return null;
        }
    }

    @Override
    public String getCurrentUsernameOrDefault(String defaultUsername) {
        try {
            if (!currentUserContext.isLogin()) {
                return defaultUsername;
            }
            return currentUserContext.getUsernameOrDefault(defaultUsername);
        } catch (SaTokenContextException exception) {
            return defaultUsername;
        } catch (Exception exception) {
            log.warn("读取当前审计用户名失败", exception);
            return defaultUsername;
        }
    }
}
