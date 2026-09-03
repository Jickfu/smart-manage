package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.vo.ResetPasswordVO;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
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
    private final UserCacheInvalidator userCacheInvalidator;
    private final RegularUserCredentialService regularUserCredentialService;
    private final AdministratorUserCredentialService administratorUserCredentialService;

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
                primaryOrganization.id(), user.getCredentialGeneration(), null);
    }

    /** 这里只按不可变用户名路由；日志与管理员身份校验位于真正的命令 Bean。 */
    public ResetPasswordVO resetPassword(Long userId) {
        UserEntity target = userMapper.selectById(userId);
        if (target == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        return UserConstant.SUPER_ADMIN.equals(target.getUsername())
                ? administratorUserCredentialService.resetPassword(userId)
                : regularUserCredentialService.resetPassword(userId);
    }

    public void changeResetPassword(Long userId, Long expectedGeneration, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能为空");
        }
        txService.changeResetPassword(userId, expectedGeneration, newPassword);
        userCacheInvalidator.tryRefreshUsers(List.of(userId));
    }
}
