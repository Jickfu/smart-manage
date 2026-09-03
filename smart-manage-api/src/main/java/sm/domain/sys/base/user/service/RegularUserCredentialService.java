package sm.domain.sys.base.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.user.model.vo.ResetPasswordVO;
import sm.system.aop.log.BizLog;
import java.util.List;

/** 可委派的普通用户密码重置命令，不允许接收 administrator 目标。 */
@Service
@RequiredArgsConstructor
public class RegularUserCredentialService {
    private final UserTxService txService;
    private final UserCacheInvalidator userCacheInvalidator;

    @BizLog(value = "重置用户密码", recordResponse = false)
    public ResetPasswordVO resetPassword(Long userId) {
        String password = txService.resetPassword(userId);
        userCacheInvalidator.tryRefreshUsers(List.of(userId));
        return new ResetPasswordVO(password);
    }
}
