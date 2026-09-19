package sm.domain.sys.base.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.AuthenticatedSessionStateVerifier;

/** 用户领域提供安全状态事实；SQL/连接异常原样交统一错误出口，不能伪装为账号错误。 */
@Component
@RequiredArgsConstructor
public class UserSessionStateVerifier implements AuthenticatedSessionStateVerifier {
    private final UserMapper mapper;

    @Override
    public void verify(Long userId, long credentialGeneration) {
        var state = mapper.selectSecurityState(userId);
        if (state == null || !Boolean.TRUE.equals(state.getEnabled())
                || state.getCredentialGeneration() == null
                || state.getCredentialGeneration() != credentialGeneration) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
    }
}
