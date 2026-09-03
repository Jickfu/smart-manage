package sm.domain.sys.base.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.user.model.vo.ResetPasswordVO;
import sm.system.aop.log.BizLog;
import sm.system.security.authorization.AdministratorOnly;
import java.util.List;

/** 真实管理员凭据命令；身份切面必须在业务日志和数据库写入之前拒绝非管理员。 */
@Service
@RequiredArgsConstructor
@AdministratorOnly
public class AdministratorUserCredentialService {
    private final UserTxService txService;
    private final UserCacheInvalidator userCacheInvalidator;

    @BizLog(value = "重置超级管理员密码", recordResponse = false)
    public ResetPasswordVO resetPassword(Long userId) {
        String password = txService.resetAdministratorPassword(userId);
        userCacheInvalidator.tryRefreshUsers(List.of(userId));
        return new ResetPasswordVO(password);
    }
}
