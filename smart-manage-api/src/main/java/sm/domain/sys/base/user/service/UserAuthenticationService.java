package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.vo.ResetPasswordVO;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.system.aop.log.BizLog;
import sm.system.auth.SessionTerminationReason;
import sm.system.exception.BizException;
import sm.system.helper.Argon2Helper;
import sm.system.response.ResultEnum;

import java.util.List;

/** 用户凭据校验与密码生命周期服务。 */
@Service
@RequiredArgsConstructor
public class UserAuthenticationService {
    private final UserMapper userMapper;
    private final UserAssignmentMapper userAssignmentMapper;
    private final OrgReferenceReader orgReferenceReader;
    private final UserTxService txService;
    private final AuthorizationStateHelper authorizationStateHelper;

    public UserAuthentication authenticate(String username, String password) {
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
        if (user == null || !Argon2Helper.verify(user.getPassword(), password)) {
            return UserAuthentication.failed("用户名或密码错误");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            return UserAuthentication.failed("用户已被禁用");
        }
        return authenticateAvailableUser(user, false);
    }

    /** 二级认证只验证当前真实管理员自己的密码。 */
    public boolean verifyAdministratorPassword(Long userId, String password) {
        UserEntity user = userMapper.selectById(userId);
        return user != null
                && UserConstant.SUPER_ADMIN.equals(user.getUsername())
                && Boolean.TRUE.equals(user.getEnabled())
                && Argon2Helper.verify(user.getPassword(), password);
    }

    /** 当前用户敏感联系方式变更前的密码二级认证。 */
    public boolean verifyCurrentPassword(Long userId, String password) {
        UserEntity user = userMapper.selectById(userId);
        return user != null && Boolean.TRUE.equals(user.getEnabled())
                && Argon2Helper.verify(user.getPassword(), password);
    }

    /** 代登录不验证目标密码，但复用正式登录的账号和主职组织有效性校验。 */
    public UserAuthentication authenticateTemporaryLogin(Long userId, String expectedUsername) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || !user.getUsername().equals(expectedUsername)
                || UserConstant.SUPER_ADMIN.equals(user.getUsername())
                || !Boolean.TRUE.equals(user.getEnabled())) {
            return UserAuthentication.failed("用户名或密码错误");
        }
        return authenticateAvailableUser(user, true);
    }

    private UserAuthentication authenticateAvailableUser(UserEntity user, boolean hideStateReason) {
        UserAssignmentEntity primaryAssignment = userAssignmentMapper.selectOne(
                new LambdaQueryWrapper<UserAssignmentEntity>()
                        .eq(UserAssignmentEntity::getUserId, user.getId())
                        .eq(UserAssignmentEntity::getIsPrimary, true));
        if (primaryAssignment == null) {
            return UserAuthentication.failed(hideStateReason
                    ? "用户名或密码错误" : "用户未配置主职组织，请联系管理员");
        }
        OrgReference primaryOrganization = orgReferenceReader.require(primaryAssignment.getOrgId());
        if (!primaryOrganization.enabled() || primaryOrganization.archived()) {
            return UserAuthentication.failed(hideStateReason
                    ? "用户名或密码错误" : "用户主职组织不可用，请联系管理员");
        }
        return new UserAuthentication(user.getId(), user.getUsername(), user.getName(),
                hideStateReason ? false : Boolean.TRUE.equals(user.getPasswordReset()),
                !hideStateReason && UserConstant.SUPER_ADMIN.equals(user.getUsername()),
                primaryOrganization.id(), null);
    }

    @BizLog(value = "重置用户密码", recordResponse = false)
    public ResetPasswordVO resetPassword(Long userId) {
        String password = txService.resetPassword(userId);
        authorizationStateHelper.terminateUsers(
                List.of(userId), SessionTerminationReason.PASSWORD_RESET_TERMINATED);
        return new ResetPasswordVO(password);
    }

    public void changeResetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能为空");
        }
        txService.changeResetPassword(userId, newPassword);
        authorizationStateHelper.terminateUsers(
                List.of(userId), SessionTerminationReason.PASSWORD_RESET_TERMINATED);
    }
}
