package sm.domain.sys.base.sysparam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.sysparam.model.form.SysParamSaveForm;
import sm.system.security.authorization.AdministratorOnly;

/** 启用公开注册需要真实管理员身份，通用系统参数权限不能单独开放注册。 */
@Service
@AdministratorOnly
@RequiredArgsConstructor
public class RegistrationPolicyAdminService {
    private final SysParamTxService txService;

    public Long save(SysParamSaveForm form) {
        return txService.saveRegistrationPolicy(form);
    }
}
