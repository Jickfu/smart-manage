package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.form.UserAssignmentForm;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.form.UserRoleAssignForm;
import sm.domain.sys.base.user.constant.UserThemeColor;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.helper.Argon2Helper;
import sm.system.util.PasswordGeneratorUtil;

import java.util.Objects;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import sm.system.util.EnabledCommandUtil;

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
    private final OrgMapper orgMapper;
    private final CurrentUserContext currentUserContext;

    /** 新增/编辑用户 */
    public Long save(UserSaveForm form) {
        return save(form, form.getId());
    }

    /** 新增时允许上层预分配ID，供头像附件绑定使用。 */
    public Long save(UserSaveForm form, Long desiredId) {
        // 检查用户名唯一性
        LambdaQueryWrapper<UserEntity> checkWrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, form.getUsername());
        if (form.getId() != null) {
            checkWrapper.ne(UserEntity::getId, form.getId());
        }
        if (mapper.selectCount(checkWrapper) > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "用户名已存在");
        }
		LambdaQueryWrapper<UserEntity> numberCheck = new LambdaQueryWrapper<UserEntity>()
				.eq(UserEntity::getNumber, form.getNumber().trim());
		if (form.getId() != null) numberCheck.ne(UserEntity::getId, form.getId());
		if (mapper.selectCount(numberCheck) > 0) {
			throw new BizException(ResultEnum.UNIQUE_CONFLICT, "工号已存在");
		}
		validateAssignments(form.getAssignments());

        UserEntity entity;
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
            }
            if (form.getVersion() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "修改用户时乐观锁版本号不能为空");
            }
            if (!Objects.equals(entity.getVersion(), form.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "用户已被其他用户修改，请刷新后重试");
            }
        } else {
            entity = new UserEntity();
            entity.setId(desiredId);
        }

		if (form.getId() != null && !Objects.equals(entity.getUsername(), form.getUsername())) {
			throw new BizException(ResultEnum.PARAM_ERROR, "登录用户名创建后不允许修改");
		}
        if (form.getId() == null) {
            entity.setUsername(form.getUsername());
        }
        // 密码只允许在新增时设置；已有用户必须通过独立重置命令修改密码。
        if (form.getId() != null && form.getPassword() != null && !form.getPassword().isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编辑用户不能修改密码，请使用重置密码");
        }
        if (form.getId() == null && form.getPassword() != null && !form.getPassword().isEmpty()) {
            entity.setPassword(Argon2Helper.encode(form.getPassword()));
        }
        entity.setName(form.getName().trim());
        entity.setNumber(form.getNumber().trim());
        entity.setGender(form.getGender());
        entity.setBirthday(form.getBirthday());
        if (form.getEmail() != null) {
            entity.setEmail(normalizeOptional(form.getEmail()));
        }
        if (form.getPhone() != null) {
            entity.setPhone(normalizeOptional(form.getPhone()));
        }
        entity.setAvatarAttachmentId(form.getAvatarAttachmentId());
        if (form.getId() == null) {
            // 新增用户：密码必填
            if (entity.getPassword() == null || entity.getPassword().isBlank()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "新增用户密码不能为空");
            }
            entity.setThemeColor(UserThemeColor.DEFAULT);
            entity.setPasswordReset(true);
			entity.setEnabled(!form.getAssignments().isEmpty());
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
			// 编辑时分配任职不会擅自启用账号，但移除全部任职必须同步禁用。
			if (form.getAssignments().isEmpty()) entity.setEnabled(false);
            if (mapper.updateById(entity) == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "用户已被其他用户修改，请刷新后重试");
            }
        }
        replaceAssignments(entity.getId(), form.getAssignments());
        return entity.getId();
    }

    /** 管理员重置密码，明文只通过本次调用返回。 */
    public String resetPassword(Long userId) {
        UserEntity entity = mapper.selectById(userId);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
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
    public void changeResetPassword(Long userId, String newPassword) {
        UserEntity entity = mapper.selectById(userId);
        if (entity == null || !Boolean.TRUE.equals(entity.getPasswordReset())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "改密状态已失效，请重新登录");
        }
        if (Argon2Helper.verify(entity.getPassword(), newPassword)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能与临时密码相同");
        }
        entity.setPassword(Argon2Helper.encode(newPassword));
        entity.setPasswordReset(false);
        if (mapper.updateById(entity) == 0) {
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
		List<OrgEntity> organizations = orgMapper.selectByIds(organizationIds);
		if (organizations.size() != organizationIds.size()
				|| organizations.stream().anyMatch(org -> !Boolean.TRUE.equals(org.getEnabled())
						|| Boolean.TRUE.equals(org.getArchived()))) {
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

    /** 当前组织下的角色关系通过独立命令整体替换。 */
    public void assignRoles(UserRoleAssignForm form) {
        if (mapper.selectById(form.getUserId()) == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        }
        Long orgId = currentUserContext.getOrgId();
        userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getUserId, form.getUserId())
                .eq(UserRoleEntity::getOrgId, orgId));
        for (Long roleId : form.getRoleIds()) {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setUserId(form.getUserId());
            userRoleEntity.setOrgId(orgId);
            userRoleEntity.setRoleId(roleId);
            if (userRoleMapper.insert(userRoleEntity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "聚合明细写入失败");
            }
        }
    }

    private String normalizeOptional(String value) {
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

	private void validateAssignments(List<UserAssignmentForm> assignments) {
		Set<Long> orgIds = new HashSet<>();
		int primaryCount = 0;
		for (UserAssignmentForm assignment : assignments) {
			if (!orgIds.add(assignment.getOrgId())) {
				throw new BizException(ResultEnum.PARAM_ERROR, "同一部门不能重复任职");
			}
			if (Boolean.TRUE.equals(assignment.getIsPrimary())) primaryCount++;
		}
		if (!assignments.isEmpty() && primaryCount != 1) {
			throw new BizException(ResultEnum.PARAM_ERROR, "存在任职时必须且只能设置一个主职");
		}
		if (!orgIds.isEmpty()) {
			List<OrgEntity> organizations = orgMapper.selectByIds(orgIds);
			if (organizations.size() != orgIds.size()
					|| organizations.stream().anyMatch(org -> !Boolean.TRUE.equals(org.getEnabled()) || Boolean.TRUE.equals(org.getArchived()))) {
				throw new BizException(ResultEnum.PARAM_ERROR, "任职部门不存在或不可用");
			}
		}
	}

	private void replaceAssignments(Long userId, List<UserAssignmentForm> assignments) {
		userAssignmentMapper.delete(new LambdaQueryWrapper<UserAssignmentEntity>()
				.eq(UserAssignmentEntity::getUserId, userId));
		for (UserAssignmentForm assignment : assignments) {
			UserAssignmentEntity entity = new UserAssignmentEntity();
			entity.setUserId(userId);
			entity.setOrgId(assignment.getOrgId());
			entity.setPosition(assignment.getPosition().trim());
			entity.setIsOrgLeader(Boolean.TRUE.equals(assignment.getIsOrgLeader()));
			entity.setIsPrimary(Boolean.TRUE.equals(assignment.getIsPrimary()));
			if (userAssignmentMapper.insert(entity) != 1) {
				throw new BizException(ResultEnum.PERSISTENCE_ERROR, "用户任职保存失败");
			}
		}
	}
}
