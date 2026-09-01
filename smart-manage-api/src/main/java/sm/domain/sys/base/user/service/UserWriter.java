package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.constant.UserThemeColor;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import sm.domain.sys.base.user.model.form.UserAssignmentForm;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.system.exception.BizException;
import sm.system.helper.Argon2Helper;
import sm.system.response.ResultEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** 用户及任职的非事务写组件；事务边界由调用它的 TxService 持有。 */
@Component
@RequiredArgsConstructor
class UserWriter {
    private final UserMapper mapper;
    private final UserRoleMapper userRoleMapper;
    private final UserAssignmentMapper userAssignmentMapper;
    private final OrgReferenceReader orgReferenceReader;

    List<Long> saveBatch(List<UserSaveForm> forms) {
        List<Long> ids = new ArrayList<>();
        for (UserSaveForm form : forms) ids.add(save(form, form.getId()));
        return ids;
    }

    Long save(UserSaveForm form, Long desiredId) {
        LambdaQueryWrapper<UserEntity> usernameCheck = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, form.getUsername())
                .ne(form.getId() != null, UserEntity::getId, form.getId());
        if (mapper.selectCount(usernameCheck) > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "用户名已存在");
        }
        LambdaQueryWrapper<UserEntity> numberCheck = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getNumber, form.getNumber().trim())
                .ne(form.getId() != null, UserEntity::getId, form.getId());
        if (mapper.selectCount(numberCheck) > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "工号已存在");
        }
        validateAssignments(form.getAssignments());
        String normalizedEmail = normalizeEmail(form.getEmail());
        String normalizedPhone = form.getPhone() == null ? null : normalizeOptional(form.getPhone());
        assertContactUnique(normalizedEmail, normalizedPhone, form.getId());

        UserEntity entity;
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
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
        if (form.getId() == null) entity.setUsername(form.getUsername());
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
        if (form.getEmail() != null && !Objects.equals(entity.getEmail(), normalizedEmail)) {
            entity.setEmail(normalizedEmail);
            entity.setEmailVerifiedAt(null);
        }
        if (form.getPhone() != null) entity.setPhone(normalizedPhone);
        entity.setAvatarAttachmentId(form.getAvatarAttachmentId());
        if (form.getId() == null) {
            if (entity.getPassword() == null || entity.getPassword().isBlank()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "新增用户密码不能为空");
            }
            entity.setThemeColor(UserThemeColor.DEFAULT);
            entity.setPasswordReset(true);
            entity.setEnabled(!form.getAssignments().isEmpty());
            if (mapper.insert(entity) != 1) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
        } else {
            if (form.getAssignments().isEmpty()) entity.setEnabled(false);
            if (mapper.updateById(entity) == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "用户已被其他用户修改，请刷新后重试");
            }
        }
        replaceAssignments(entity.getId(), form.getAssignments());
        return entity.getId();
    }

    private void assertContactUnique(String email, String phone, Long excludedUserId) {
        if (email != null) {
            LambdaQueryWrapper<UserEntity> emailQuery = new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getEmail, email)
                    .ne(excludedUserId != null, UserEntity::getId, excludedUserId);
            if (mapper.selectCount(emailQuery) > 0) throw new BizException(ResultEnum.UNIQUE_CONFLICT, "邮箱已被其他账号使用");
        }
        if (phone != null) {
            LambdaQueryWrapper<UserEntity> phoneQuery = new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getPhone, phone)
                    .ne(excludedUserId != null, UserEntity::getId, excludedUserId);
            if (mapper.selectCount(phoneQuery) > 0) throw new BizException(ResultEnum.UNIQUE_CONFLICT, "手机号已被其他账号使用");
        }
    }

    private String normalizeEmail(String value) {
        if (value == null) return null;
        String normalized = normalizeOptional(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateAssignments(List<UserAssignmentForm> assignments) {
        Set<Long> orgIds = new HashSet<>();
        int primaryCount = 0;
        for (UserAssignmentForm assignment : assignments) {
            if (!orgIds.add(assignment.getOrgId())) throw new BizException(ResultEnum.PARAM_ERROR, "同一部门不能重复任职");
            if (Boolean.TRUE.equals(assignment.getIsPrimary())) primaryCount++;
        }
        if (!assignments.isEmpty() && primaryCount != 1) {
            throw new BizException(ResultEnum.PARAM_ERROR, "存在任职时必须且只能设置一个主职");
        }
        if (!orgIds.isEmpty()) {
            var organizations = orgReferenceReader.findByIds(orgIds);
            if (organizations.size() != orgIds.size()
                    || organizations.values().stream().anyMatch(org -> !org.enabled() || org.archived())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "任职部门不存在或不可用");
            }
        }
    }

    private void replaceAssignments(Long userId, List<UserAssignmentForm> assignments) {
        Set<Long> nextOrgIds = new HashSet<>();
        for (UserAssignmentForm assignment : assignments) nextOrgIds.add(assignment.getOrgId());
        List<UserAssignmentEntity> previousAssignments = userAssignmentMapper.selectList(
                new LambdaQueryWrapper<UserAssignmentEntity>().eq(UserAssignmentEntity::getUserId, userId));
        Set<Long> removedOrgIds = new HashSet<>();
        for (UserAssignmentEntity previousAssignment : previousAssignments) {
            if (!nextOrgIds.contains(previousAssignment.getOrgId())) removedOrgIds.add(previousAssignment.getOrgId());
        }
        // 任职和角色必须保持一致，避免重新任职时历史授权自动恢复。
        if (!removedOrgIds.isEmpty()) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>()
                    .eq(UserRoleEntity::getUserId, userId).in(UserRoleEntity::getOrgId, removedOrgIds));
        }
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
