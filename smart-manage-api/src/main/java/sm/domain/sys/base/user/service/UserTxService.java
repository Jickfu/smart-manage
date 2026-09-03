package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.form.UserAssignmentForm;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.form.UserRoleAssignmentSaveForm;
import sm.domain.sys.base.user.constant.UserThemeColor;
import sm.domain.sys.base.user.model.Gender;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.helper.Argon2Helper;
import sm.system.util.PasswordGeneratorUtil;

import java.util.Objects;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import sm.system.util.EnabledCommandUtil;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.user.model.UserCredentialSnapshot;

/**
 * 用户事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class UserTxService {
    private final UserMapper mapper;
    private final UserRoleMapper userRoleMapper;
    private final UserAssignmentMapper userAssignmentMapper;
    private final OrgReferenceReader orgReferenceReader;
    private final CurrentUserContext currentUserContext;
    private final UserWriter userWriter;

    /** 新增/编辑用户 */
    public Long save(UserSaveForm form) {
        return userWriter.save(form, form.getId());
    }

    /** 新增时允许上层预分配ID，供头像附件绑定使用。 */
    public Long save(UserSaveForm form, Long desiredId) {
        return userWriter.save(form, desiredId);
    }

    /** 普通目标重置路径不得接收超级管理员，即使未来上层路由有误也必须拒绝。 */
    public String resetPassword(Long userId) {
        return resetPasswordTarget(userId, false);
    }

    /** 只能由独立 @AdministratorOnly 命令边界调用。 */
    public String resetAdministratorPassword(Long userId) {
        return resetPasswordTarget(userId, true);
    }

    private String resetPasswordTarget(Long userId, boolean administratorTarget) {
        UserEntity entity = mapper.selectById(userId);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        }
        if (UserConstant.SUPER_ADMIN.equals(entity.getUsername()) != administratorTarget) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "目标用户不属于此凭据重置入口");
        }
        String password = PasswordGeneratorUtil.generate(12);
        entity.setPassword(Argon2Helper.encode(password));
        entity.setPasswordReset(true);
        if (mapper.updateById(entity) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "用户信息已变化，请刷新后重试");
        }
        return password;
    }

    /** 使用一次性改单凭证设置正式密码。 */
    public void changeResetPassword(Long userId, Long expectedGeneration, String newPassword) {
        UserEntity entity = mapper.selectById(userId);
        if (entity == null || !Boolean.TRUE.equals(entity.getPasswordReset())
                || !Boolean.TRUE.equals(entity.getEnabled()) || expectedGeneration == null
                || !expectedGeneration.equals(entity.getCredentialGeneration())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "改密状态已失效，请重新登录");
        }
        if (Argon2Helper.verify(entity.getPassword(), newPassword)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能与临时密码相同");
        }
        if (mapper.changeResetPassword(userId, expectedGeneration, Argon2Helper.encode(newPassword)) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "用户信息已变化，请重新登录");
        }
    }

    /** 只更新当前用户的个人主题配置，不允许借此修改其他资料。 */
    public void updateCurrentTheme(Long userId, String themeColor) {
        UserEntity entity = mapper.selectById(userId);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        }
        entity.setThemeColor(UserThemeColor.normalizeRequired(themeColor));
        if (mapper.updateById(entity) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "用户信息已变化，请刷新后重试");
        }
    }

    /** 只更新当前用户允许自行维护的基础资料。 */
    public void updateCurrentProfile(Long userId, String name, Gender gender, LocalDate birthday,
            Long avatarAttachmentId) {
        UserEntity entity = mapper.selectById(userId);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        entity.setName(name.trim());
        entity.setGender(gender);
        entity.setBirthday(birthday);
        entity.setAvatarAttachmentId(avatarAttachmentId);
        if (mapper.updateById(entity) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "用户信息已变化，请刷新后重试");
        }
    }

    /** 联系方式修改必须在同一事务内验证当前密码并写入，避免验证结果被重放。 */
    public void updateCurrentContact(Long userId, String password, String type, String value) {
        UserEntity entity = mapper.selectById(userId);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        if (!Argon2Helper.verify(entity.getPassword(), password)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码不正确");
        }
        String normalizedValue = value.trim();
        if ("PHONE".equals(type)) {
            assertContactUnique(null, normalizedValue, userId);
            entity.setPhone(normalizedValue);
        } else if ("EMAIL".equals(type)) {
            if (!normalizedValue.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new BizException(ResultEnum.PARAM_ERROR, "邮箱格式不正确");
            }
            entity.setEmail(normalizedValue);
        } else {
            throw new BizException(ResultEnum.PARAM_ERROR, "联系方式类型无效");
        }
        try {
            if (mapper.updateById(entity) == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "用户信息已变化，请刷新后重试");
            }
        } catch (DuplicateKeyException exception) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "手机号或邮箱已被其他账号使用");
        }
    }

    /** 验证原密码后修改密码，避免已登录会话被用于静默接管账号。 */
    public void updateCurrentPassword(Long userId, String currentPassword, String newPassword) {
        UserEntity entity = mapper.selectById(userId);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        if (!Argon2Helper.verify(entity.getPassword(), currentPassword)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "原密码不正确");
        }
        if (Argon2Helper.verify(entity.getPassword(), newPassword)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能与原密码相同");
        }
        entity.setPassword(Argon2Helper.encode(newPassword));
        entity.setPasswordReset(false);
        if (mapper.updateById(entity) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "用户信息已变化，请刷新后重试");
        }
    }

    /** 邮箱验证码已经在事务外原子消费，本事务只完成密码安全事件写入。 */
    public void updatePasswordByVerifiedEmail(UserCredentialSnapshot snapshot, String newPassword) {
        UserEntity entity = mapper.selectById(snapshot.userId());
        if (entity == null || entity.getEmailVerifiedAt() == null || !Boolean.TRUE.equals(entity.getEnabled())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "邮箱验证状态已变化，请重新获取验证码");
        }
        if (Argon2Helper.verify(entity.getPassword(), newPassword)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能与原密码相同");
        }
        // 最终 SQL 必须再次比较签发快照；事务外消费成功不等于此刻仍有改密权限。
        if (mapper.updatePasswordByVerifiedEmail(snapshot, Argon2Helper.encode(newPassword)) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "用户信息已变化，请重新获取验证码");
        }
    }

    /** 验证码发送到新邮箱并成功消费后，才正式绑定并标记邮箱已验证。 */
    public void bindVerifiedEmail(UserCredentialSnapshot snapshot, String email) {
        String normalizedEmail = normalizeEmail(email);
        assertContactUnique(normalizedEmail, null, snapshot.userId());
        try {
            if (mapper.bindVerifiedEmail(snapshot, normalizedEmail) != 1) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "用户信息已变化，请重新验证邮箱");
            }
        } catch (DuplicateKeyException exception) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "邮箱已被其他账号使用");
        }
    }

    private void assertContactUnique(String email, String phone, Long excludedUserId) {
        if (email != null) {
            LambdaQueryWrapper<UserEntity> emailQuery = new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getEmail, email)
                    .ne(excludedUserId != null, UserEntity::getId, excludedUserId);
            if (mapper.selectCount(emailQuery) > 0) {
                throw new BizException(ResultEnum.UNIQUE_CONFLICT, "邮箱已被其他账号使用");
            }
        }
        if (phone != null) {
            LambdaQueryWrapper<UserEntity> phoneQuery = new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getPhone, phone)
                    .ne(excludedUserId != null, UserEntity::getId, excludedUserId);
            if (mapper.selectCount(phoneQuery) > 0) {
                throw new BizException(ResultEnum.UNIQUE_CONFLICT, "手机号已被其他账号使用");
            }
        }
    }

    private String normalizeEmail(String value) {
        if (value == null) return null;
        String normalizedValue = normalizeOptional(value);
        return normalizedValue == null ? null : normalizedValue.toLowerCase(Locale.ROOT);
    }

    public void updateEnabled(List<Long> ids, boolean enabled) {
        if (!enabled && ids.contains(currentUserContext.getUserId())) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR, "不能禁用当前登录用户");
        }
		if (enabled) validateEnabledUserAssignments(ids);
        EnabledCommandUtil.update(mapper, UserEntity::getId, UserEntity::getEnabled, ids, enabled, "用户");
    }

	/** 启用账号前必须确认每个用户都有唯一且可用的主职组织。 */
	private void validateEnabledUserAssignments(List<Long> userIds) {
		List<UserAssignmentEntity> primaryAssignments = userAssignmentMapper.selectList(
				new LambdaQueryWrapper<UserAssignmentEntity>()
						.in(UserAssignmentEntity::getUserId, userIds)
						.eq(UserAssignmentEntity::getIsPrimary, true));
		Set<Long> assignedUserIds = new HashSet<>();
		Set<Long> organizationIds = new HashSet<>();
		for (UserAssignmentEntity assignment : primaryAssignments) {
			assignedUserIds.add(assignment.getUserId());
			organizationIds.add(assignment.getOrgId());
		}
		if (!assignedUserIds.containsAll(userIds)) {
			throw new BizException(ResultEnum.BILL_STATUS_ERROR, "启用用户前必须配置主职组织");
		}
		var organizations = orgReferenceReader.findByIds(organizationIds);
		if (organizations.size() != organizationIds.size()
				|| organizations.values().stream().anyMatch(org -> !org.enabled() || org.archived())) {
			throw new BizException(ResultEnum.BILL_STATUS_ERROR, "用户主职组织不存在或不可用");
		}
	}

    /** 删除用户 */
    public void deleteById(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "用户ID不能为空");
        }
        // 不能删除自己
        if (id.equals(currentUserContext.getUserId())) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR, "不能删除当前登录用户");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getUserId, id));
        userAssignmentMapper.delete(new LambdaQueryWrapper<UserAssignmentEntity>()
                .eq(UserAssignmentEntity::getUserId, id));
        if (mapper.deleteById(id) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "用户不存在或已被删除");
        }
    }

	/** 全部任职组织的角色关系通过一个事务整体替换。 */
	public void saveRoleAssignment(UserRoleAssignmentSaveForm form) {
		if (mapper.selectById(form.getUserId()) == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
		}
		List<UserAssignmentEntity> userAssignments = userAssignmentMapper.selectList(
				new LambdaQueryWrapper<UserAssignmentEntity>()
						.eq(UserAssignmentEntity::getUserId, form.getUserId()));
		Set<Long> allowedOrgIds = new HashSet<>();
		for (UserAssignmentEntity assignment : userAssignments) allowedOrgIds.add(assignment.getOrgId());
		Set<Long> submittedOrgIds = new HashSet<>();
		Set<Long> submittedRoleIds = new HashSet<>();
		for (var assignment : form.getAssignments()) {
			if (!submittedOrgIds.add(assignment.getOrgId())) {
				throw new BizException(ResultEnum.PARAM_ERROR, "同一任职组织不能重复提交");
			}
			if (!allowedOrgIds.contains(assignment.getOrgId())) {
				throw new BizException(ResultEnum.PARAM_ERROR, "只能为用户任职组织分配角色");
			}
			if (new HashSet<>(assignment.getRoleIds()).size() != assignment.getRoleIds().size()) {
				throw new BizException(ResultEnum.PARAM_ERROR, "同一组织下不能重复分配角色");
			}
			submittedRoleIds.addAll(assignment.getRoleIds());
		}
		if (!submittedRoleIds.isEmpty()
				&& userRoleMapper.selectExistingRoleIds(submittedRoleIds).size() != submittedRoleIds.size()) {
			throw new BizException(ResultEnum.PARAM_ERROR, "角色不存在或已被删除");
		}
		userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>()
				.eq(UserRoleEntity::getUserId, form.getUserId()));
		for (var assignment : form.getAssignments()) {
			for (Long roleId : assignment.getRoleIds()) {
				UserRoleEntity relation = new UserRoleEntity();
				relation.setUserId(form.getUserId());
				relation.setOrgId(assignment.getOrgId());
				relation.setRoleId(roleId);
				if (userRoleMapper.insert(relation) != 1) {
					throw new BizException(ResultEnum.PERSISTENCE_ERROR, "用户角色关系写入失败");
				}
			}
		}
	}

    private String normalizeOptional(String value) {
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
