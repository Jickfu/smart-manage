package sm.domain.sys.base.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.base.user.model.form.UserAssignmentForm;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class UserRegistrationTxService {
    private final SysParamService sysParamService;
    private final UserWriter userWriter;

    public Long register(String username, String email, String password) {
        var policy = sysParamService.getRegistrationPolicy();
        if (!policy.enabled()) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "邮箱注册暂不可用");
        }
        UserSaveForm form = new UserSaveForm();
        form.setUsername(username);
        form.setName("新用户");
        form.setNumber(username);
        form.setEmail(email);
        form.setPassword(password);
        if (policy.orgId() == null) {
            form.setAssignments(List.of());
        } else {
            UserAssignmentForm assignment = new UserAssignmentForm();
            assignment.setOrgId(policy.orgId());
            assignment.setPosition(policy.position());
            assignment.setIsPrimary(true);
            form.setAssignments(List.of(assignment));
        }
        return userWriter.register(form);
    }
}
