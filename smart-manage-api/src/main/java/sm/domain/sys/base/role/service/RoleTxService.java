package sm.domain.sys.base.role.service;

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
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeOrgMapper;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeEntity;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeOrgEntity;
import sm.domain.sys.base.role.model.form.RoleDataScopeAssignForm;
import sm.system.resource.BusinessResourceRegistry;

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
    private final RolePermissionMapper permissionMapper;
    private final RoleDataScopeMapper dataScopeMapper;
    private final RoleDataScopeOrgMapper dataScopeOrgMapper;
    private final BusinessResourceRegistry resourceRegistry;

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
            entity.setDefaultDataScope("admin".equals(entity.getNumber()) ? "ALL" : "SELF");
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
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "角色ID不能为空");
        }
        RoleEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "角色不存在");
        }
        permissionMapper.delete(new LambdaQueryWrapper<RolePermissionEntity>()
                .eq(RolePermissionEntity::getRoleId, id));
        if (mapper.deleteById(id) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "角色已被其他用户删除");
        }
    }

    /** 权限关系通过独立命令整体替换，不与角色资料保存耦合。 */
    public void assignPermissions(RolePermissionAssignForm form) {
        if (mapper.selectById(form.getRoleId()) == null) {
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
        if ("admin".equals(role.getNumber())) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "系统管理员固定拥有全部数据范围");
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

        var oldRules = dataScopeMapper.selectList(new LambdaQueryWrapper<RoleDataScopeEntity>()
                .eq(RoleDataScopeEntity::getRoleId, role.getId()));
        if (!oldRules.isEmpty()) {
            dataScopeOrgMapper.delete(new LambdaQueryWrapper<RoleDataScopeOrgEntity>()
                    .in(RoleDataScopeOrgEntity::getScopeRuleId, oldRules.stream().map(RoleDataScopeEntity::getId).toList()));
        }
        dataScopeMapper.delete(new LambdaQueryWrapper<RoleDataScopeEntity>().eq(RoleDataScopeEntity::getRoleId, role.getId()));
        for (var ruleForm : form.getRules()) {
            var actions = resourceRegistry.dataScopeActions(ruleForm.getResourceType());
            if (ruleForm.getAction() != null && !actions.contains(ruleForm.getAction())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "业务资源未声明数据操作: " + ruleForm.getAction());
            }
            if ("CUSTOM_ORGS".equals(ruleForm.getScopeType()) && ruleForm.getOrgIds().isEmpty()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "自定义组织范围不能为空");
            }
            RoleDataScopeEntity rule = new RoleDataScopeEntity();
            rule.setRoleId(role.getId());
            rule.setResourceType(ruleForm.getResourceType());
            rule.setAction(ruleForm.getAction());
            rule.setScopeType(ruleForm.getScopeType());
            if (dataScopeMapper.insert(rule) != 1) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "数据范围规则写入失败");
            if ("CUSTOM_ORGS".equals(ruleForm.getScopeType())) {
                for (Long orgId : ruleForm.getOrgIds().stream().distinct().toList()) {
                    RoleDataScopeOrgEntity relation = new RoleDataScopeOrgEntity();
                    relation.setScopeRuleId(rule.getId());
                    relation.setOrgId(orgId);
                    if (dataScopeOrgMapper.insert(relation) != 1) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "自定义组织写入失败");
                }
            }
        }
    }
}
