package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
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
    private final CurrentUserContext currentUserContext;

    /** 新增/编辑用户 */
    public Long save(UserSaveForm form) {
        // 检查用户名唯一性
        LambdaQueryWrapper<UserEntity> checkWrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, form.getUsername());
        if (form.getId() != null) {
            checkWrapper.ne(UserEntity::getId, form.getId());
        }
        if (mapper.selectCount(checkWrapper) > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "用户名已存在");
        }

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
        }

		if (form.getId() != null && !Objects.equals(entity.getUsername(), form.getUsername())) {
			throw new BizException(ResultEnum.PARAM_ERROR, "登录用户名创建后不允许修改");
		}
        entity.setUsername(form.getUsername());
        // 密码只允许在新增时设置；已有用户必须通过独立重置命令修改密码。
        if (form.getId() != null && form.getPassword() != null && !form.getPassword().isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编辑用户不能修改密码，请使用重置密码");
        }
        if (form.getId() == null && form.getPassword() != null && !form.getPassword().isEmpty()) {
            entity.setPassword(Argon2Helper.encode(form.getPassword()));
        }
        if (form.getNickname() != null) {
            entity.setNickname(form.getNickname());
        }
        if (form.getEmail() != null) {
            entity.setEmail(normalizeOptional(form.getEmail()));
        }
        if (form.getPhone() != null) {
            entity.setPhone(normalizeOptional(form.getPhone()));
        }
        if (form.getAvatar() != null) {
            entity.setAvatar(form.getAvatar());
        }
        if (form.getId() == null) {
            // 新增用户：密码必填
            if (entity.getPassword() == null || entity.getPassword().isBlank()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "新增用户密码不能为空");
            }
            entity.setEnabled(true);
            entity.setThemeColor(UserThemeColor.DEFAULT);
            entity.setPasswordReset(true);
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            if (mapper.updateById(entity) == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "用户已被其他用户修改，请刷新后重试");
            }
        }
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
        EnabledCommandUtil.update(mapper, UserEntity::getId, UserEntity::getEnabled, ids, enabled, "用户");
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
}
