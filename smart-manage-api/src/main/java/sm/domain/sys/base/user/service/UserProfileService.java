package sm.domain.sys.base.user.service;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.attachment.contract.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.OrgType;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.UserCacheSnapshot;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.CurrentUserContactForm;
import sm.domain.sys.base.user.model.form.CurrentUserPasswordForm;
import sm.domain.sys.base.user.model.form.CurrentUserProfileForm;
import sm.domain.sys.base.user.model.vo.UserAssignmentVO;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.system.aop.log.BizLog;
import sm.system.auth.SessionTerminationReason;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.crypto.BrowserPasswordCipher;
import sm.system.security.crypto.Sm2CiphertextException;
import sm.system.security.context.CurrentUserContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 当前用户资料、组织上下文和头像生命周期服务。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileService {
    private final UserMapper userMapper;
    private final UserAssignmentMapper userAssignmentMapper;
    private final OrgMapper orgMapper;
    private final AttachmentService attachmentService;
    private final UserTxService txService;
    private final AuthorizationStateHelper authorizationStateHelper;
    private final UserCacheAccessor userCacheAccessor;
    private final UserConverter converter;
    private final CurrentUserContext currentUserContext;
    private final BrowserPasswordCipher browserPasswordCipher;

    public AttachmentEntity requireAvatar(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getAvatarAttachmentId() == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "用户头像未设置");
        }
        return attachmentService.requireAggregateAttachment(user.getAvatarAttachmentId(),
                UserResourceRegistration.RESOURCE_TYPE, String.valueOf(userId));
    }

    public UserInfoVO current() {
        Long userId = currentUserContext.getUserId();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) invalidateCurrentSession(userId, SessionTerminationReason.ACCOUNT_DELETED);
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            invalidateCurrentSession(userId, SessionTerminationReason.ACCOUNT_DISABLED);
        }
        UserInfoVO result = converter.toInfoVO(user);
        result.setAvatar(avatarUrl(user.getId(), user.getAvatarAttachmentId()));
        assembleCurrentOrganization(result, user.getId());
        return result;
    }

    private void invalidateCurrentSession(Long userId, SessionTerminationReason reason) {
        authorizationStateHelper.terminateUsers(List.of(userId), reason);
        throw new BizException(ResultEnum.UNAUTHORIZED, "登录状态已失效，请重新登录");
    }

    public void switchCurrentOrganization(Long orgId) {
        Long userId = currentUserContext.getUserId();
        UserAssignmentEntity assignment = userAssignmentMapper.selectOne(
                new LambdaQueryWrapper<UserAssignmentEntity>()
                        .eq(UserAssignmentEntity::getUserId, userId)
                        .eq(UserAssignmentEntity::getOrgId, orgId));
        if (assignment == null) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "只能切换到自己的任职组织");
        }
        OrgEntity organization = orgMapper.selectById(orgId);
        if (organization == null || !Boolean.TRUE.equals(organization.getEnabled())
                || Boolean.TRUE.equals(organization.getArchived())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "目标组织不可用");
        }
        currentUserContext.setOrgId(orgId);
    }

    @BizLog("修改个人资料")
    @CacheInvalidate(name = BaseCacheName.USER_INFO, key = "@currentUserContext.getUserId()")
    public void updateCurrentProfile(CurrentUserProfileForm form) {
        Long userId = currentUserContext.getUserId();
        UserCacheSnapshot previous = userCacheAccessor.requireUser(userId);
        Long temporaryAvatarId = findTemporaryAvatarId(form.getAvatarAttachmentId());
        promoteAvatar(form.getAvatarAttachmentId(), form.getAttachmentUploadSessions(), userId);
        try {
            txService.updateCurrentProfile(userId, form.getName(), form.getGender(), form.getBirthday(),
                    form.getAvatarAttachmentId());
            deleteReplacedAvatar(previous.getAvatarAttachmentId(), form.getAvatarAttachmentId());
        } catch (RuntimeException exception) {
            deleteAvatarForCompensation(temporaryAvatarId);
            throw exception;
        }
    }

    @BizLog(value = "修改个人联系方式", recordResponse = false)
    @CacheInvalidate(name = BaseCacheName.USER_INFO, key = "@currentUserContext.getUserId()")
    public void updateCurrentContact(CurrentUserContactForm form) {
        if ("EMAIL".equals(form.getType())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "邮箱必须通过发送到新地址的验证码完成绑定");
        }
        String password;
        try {
            password = browserPasswordCipher.decrypt(form.getPassword());
        } catch (Sm2CiphertextException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码加密数据无效");
        }
        txService.updateCurrentContact(currentUserContext.getUserId(), password, form.getType(), form.getValue());
    }

    @BizLog(value = "修改个人密码", recordResponse = false)
    public void updateCurrentPassword(CurrentUserPasswordForm form) {
        String currentPassword;
        String newPassword;
        try {
            currentPassword = browserPasswordCipher.decrypt(form.getCurrentPassword());
            newPassword = browserPasswordCipher.decrypt(form.getNewPassword());
        } catch (Sm2CiphertextException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码加密数据无效");
        }
        if (newPassword.length() < 8) {
            throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能少于8位");
        }
        Long userId = currentUserContext.getUserId();
        txService.updateCurrentPassword(userId, currentPassword, newPassword);
        authorizationStateHelper.terminateUsers(
                List.of(userId), SessionTerminationReason.PASSWORD_RESET_TERMINATED);
    }

    @BizLog("修改个人主题")
    @CacheInvalidate(name = BaseCacheName.USER_INFO, key = "@currentUserContext.getUserId()")
    public void updateCurrentTheme(String themeColor) {
        txService.updateCurrentTheme(currentUserContext.getUserId(), themeColor);
    }

    private void assembleCurrentOrganization(UserInfoVO result, Long userId) {
        List<UserAssignmentEntity> assignments = userAssignmentMapper.selectList(
                new LambdaQueryWrapper<UserAssignmentEntity>()
                        .eq(UserAssignmentEntity::getUserId, userId)
                        .orderByDesc(UserAssignmentEntity::getIsPrimary)
                        .orderByAsc(UserAssignmentEntity::getOrgId));
        Set<Long> organizationIds = assignments.stream().map(UserAssignmentEntity::getOrgId).collect(Collectors.toSet());
        Map<Long, OrgEntity> organizationById = new HashMap<>();
        if (!organizationIds.isEmpty()) {
            for (OrgEntity organization : orgMapper.selectByIds(organizationIds)) {
                organizationById.put(organization.getId(), organization);
            }
        }
        List<UserAssignmentVO> availableAssignments = new ArrayList<>();
        Map<Long, OrgEntity> availableOrganizations = new HashMap<>();
        for (UserAssignmentEntity assignment : assignments) {
            OrgEntity organization = organizationById.get(assignment.getOrgId());
            if (organization == null || !Boolean.TRUE.equals(organization.getEnabled())
                    || Boolean.TRUE.equals(organization.getArchived())) continue;
            availableOrganizations.put(organization.getId(), organization);
            UserAssignmentVO assignmentVO = new UserAssignmentVO();
            assignmentVO.setId(assignment.getId());
            assignmentVO.setOrg(new ReferenceVO(organization.getId(), organization.getNumber(), organization.getName()));
            assignmentVO.setOrgNamePath(organization.getNamePath());
            assignmentVO.setPosition(assignment.getPosition());
            assignmentVO.setIsOrgLeader(assignment.getIsOrgLeader());
            assignmentVO.setIsPrimary(assignment.getIsPrimary());
            availableAssignments.add(assignmentVO);
        }
        OrgEntity currentOrganization = availableOrganizations.get(currentUserContext.getOrgId());
        if (currentOrganization == null) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "当前组织不在用户的有效任职范围内");
        }
        result.setAssignments(availableAssignments);
        result.setCurrentOrgId(currentOrganization.getId());
        result.setCurrentOrgName(currentOrganization.getName());
        assembleOrganizationNames(result, currentOrganization);
    }

    /** 一次向上遍历同时解析最近公司和组织树绝对顶层，避免为两个名称重复查询父链。 */
    private void assembleOrganizationNames(UserInfoVO result, OrgEntity organization) {
        OrgEntity current = organization;
        String rootOrganizationName = organization.getName();
        String companyName = null;
        while (current != null) {
            rootOrganizationName = current.getName();
            if (companyName == null && OrgType.COMPANY.equals(current.getOrgType())) {
                companyName = current.getName();
            }
            current = current.getParentId() == null ? null : orgMapper.selectById(current.getParentId());
        }
        result.setCompanyName(companyName == null ? rootOrganizationName : companyName);
        result.setRootOrgName(rootOrganizationName);
    }

    private String avatarUrl(Long userId, Long attachmentId) {
        return attachmentId == null ? null : "/sys/base/user/avatar/" + userId + "?v=" + attachmentId;
    }

    private void promoteAvatar(Long attachmentId, Map<Long, String> uploadSessions, Long userId) {
        if (attachmentId == null || !uploadSessions.containsKey(attachmentId)) return;
        AttachmentPromoteForm form = new AttachmentPromoteForm();
        form.setAttachmentIds(List.of(attachmentId));
        form.setBizType(UserResourceRegistration.RESOURCE_TYPE);
        form.setBizId(String.valueOf(userId));
        form.setUploadSessions(uploadSessions);
        try {
            attachmentService.promoteForAggregate(form);
        } catch (IOException exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "用户头像确认失败: " + exception.getMessage());
        }
    }

    private Long findTemporaryAvatarId(Long attachmentId) {
        if (attachmentId == null) return null;
        return attachmentService.listByIds(List.of(attachmentId)).stream()
                .filter(attachment -> Boolean.TRUE.equals(attachment.getIsTemp()))
                .map(sm.domain.sys.base.attachment.contract.model.vo.AttachmentVO::getId)
                .findFirst().orElse(null);
    }

    private void deleteReplacedAvatar(Long previousId, Long nextId) {
        if (previousId == null || previousId.equals(nextId)) return;
        deleteAvatarForCompensation(previousId);
    }

    private void deleteAvatarForCompensation(Long attachmentId) {
        if (attachmentId == null) return;
        try {
            attachmentService.deleteForAggregate(attachmentId);
        } catch (IOException | RuntimeException exception) {
            log.warn("用户头像清理失败，需按附件ID重试: id={}", attachmentId, exception);
        }
    }
}
