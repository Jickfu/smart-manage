package sm.domain.sys.base.role.service;

import sm.domain.sys.base.role.model.form.RoleDefaultSaveForm;
import sm.domain.sys.base.role.mapper.RoleDefaultMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.role.model.entity.RoleEntity;
import sm.domain.sys.base.role.model.form.RoleSaveForm;
import sm.domain.sys.base.role.model.form.RolePermissionAssignForm;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;
import sm.domain.sys.base.role.model.entity.RolePermissionEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.domain.sys.base.datascope.model.DataScopeRuleSnapshot;
import sm.domain.sys.base.datascope.service.DataScopeConfigurationService;
import sm.domain.sys.base.role.model.form.RoleDataScopeAssignForm;

import java.util.Objects;
import java.util.HashSet;

/**
 * 角色事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class RoleTxService {
    private final RoleMapper mapper;
    private final RoleDefaultMapper defaultMapper;
    private final RolePermissionMapper permissionMapper;
    private final DataScopeConfigurationService dataScopeConfigurationService;

    public void setDefault(RoleDefaultSaveForm form) {
        var configuration = defaultMapper.selectForUpdate();
        if (configuration == null) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "默认角色配置缺失");
        if (!Objects.equals(configuration.getVersion(), form.getVersion())
                || !Objects.equals(configuration.getRoleId(), form.getPreviousRoleId())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "默认角色已发生变化，请刷新后重试");
        }
        if (form.getRoleId() != null && mapper.selectForUpdate(form.getRoleId()) == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "角色不存在");
        }
        if (Objects.equals(configuration.getRoleId(), form.getRoleId())) return;
        // 显式更新空值以支持取消；不得写入或删除任何手工用户角色关系。
        if (defaultMapper.updateRole(form.getRoleId(), form.getVersion()) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "默认角色已发生变化，请刷新后重试");
        }
    }

    public Long save(RoleSaveForm form) {
        // 检查角色编码唯一性
        LambdaQueryWrapper<RoleEntity> checkWrapper = new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getNumber, form.getNumber());
        if (form.getId() != null) {
            checkWrapper.ne(RoleEntity::getId, form.getId());
        }
        if (mapper.selectCount(checkWrapper) > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "角色编码已存在");
        }

        RoleEntity entity;
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "角色不存在");
            }
            if (form.getVersion() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "修改角色时乐观锁版本号不能为空");
            }
            if (!Objects.equals(entity.getVersion(), form.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "角色已被其他用户修改，请刷新后重试");
            }
        } else {
            entity = new RoleEntity();
        }
        entity.setName(form.getName());
        entity.setNumber(form.getNumber());
        entity.setDescription(form.getDescription());
        // 角色资料保存不能修改数据权限；新角色从最小范围开始，后续仅由专用命令调整。
        if (form.getId() == null) {
            entity.setDefaultDataScope("SELF");
        }

        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            if (mapper.updateById(entity) == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "角色已被其他用户修改，请刷新后重试");
            }
        }
        return entity.getId();
    }

    public void deleteById(Long id) {
        if (id == null) throw new BizException(ResultEnum.PARAM_ERROR, "角色ID不能为空");
        deleteByIds(java.util.List.of(id));
    }

    public void deleteByIds(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(Objects::isNull)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "角色ID列表不能为空或包含空值");
        }
        // 与设置命令统一先锁配置，再按固定顺序锁角色；整批拒绝或整批提交。
        var configuration = defaultMapper.selectForUpdate();
        if (configuration == null) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "默认角色配置缺失");
        if (configuration.getRoleId() != null && ids.contains(configuration.getRoleId())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "请先取消或切换默认角色，再删除该角色");
        }
        var roleIds = ids.stream().distinct().sorted().toList();
        for (Long roleId : roleIds) {
            if (mapper.selectForUpdate(roleId) == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "角色不存在");
            }
        }
        for (Long roleId : roleIds) {
            permissionMapper.delete(new LambdaQueryWrapper<RolePermissionEntity>()
                    .eq(RolePermissionEntity::getRoleId, roleId));
            if (mapper.deleteById(roleId) != 1) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "角色已被其他用户删除");
            }
        }
    }

    /** 权限关系通过独立命令整体替换，不与角色资料保存耦合。 */
    public void assignPermissions(RolePermissionAssignForm form) {
        if (mapper.selectForUpdate(form.getRoleId()) == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "角色不存在");
        }
        permissionMapper.delete(new LambdaQueryWrapper<RolePermissionEntity>()
                .eq(RolePermissionEntity::getRoleId, form.getRoleId()));
        for (Long permissionId : form.getPermissionIds()) {
            RolePermissionEntity permissionEntity = new RolePermissionEntity();
            permissionEntity.setRoleId(form.getRoleId());
            permissionEntity.setPermissionId(permissionId);
            if (permissionMapper.insert(permissionEntity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "聚合明细写入失败");
            }
        }
    }

    public void assignDataScopes(RoleDataScopeAssignForm form) {
        RoleEntity role = mapper.selectById(form.getRoleId());
        if (role == null) throw new BizException(ResultEnum.NOT_FOUND, "角色不存在");
        if (!Objects.equals(role.getVersion(), form.getVersion())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "角色已被其他用户修改，请刷新后重试");
        }
        var uniqueRules = new HashSet<String>();
        for (var ruleForm : form.getRules()) {
            String ruleKey = ruleForm.getResourceType() + "\u0000" + Objects.toString(ruleForm.getAction(), "");
            if (!uniqueRules.add(ruleKey)) {
                throw new BizException(ResultEnum.PARAM_ERROR, "同一资源操作只能配置一条数据范围规则");
            }
        }
        role.setDefaultDataScope(form.getDefaultDataScope());
        if (mapper.updateById(role) != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "角色已被其他用户修改");

        var ruleSnapshots = new java.util.ArrayList<DataScopeRuleSnapshot>();
        for (var ruleForm : form.getRules()) {
            if ("CUSTOM_ORGS".equals(ruleForm.getScopeType()) && ruleForm.getOrgIds().isEmpty()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "自定义组织范围不能为空");
            }
            ruleSnapshots.add(new DataScopeRuleSnapshot(ruleForm.getResourceType(), ruleForm.getAction(),
                    ruleForm.getScopeType(), ruleForm.getOrgIds()));
        }
        dataScopeConfigurationService.replaceRoleRules(role.getId(), ruleSnapshots);
    }
}
