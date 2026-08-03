package sm.domain.sys.base.menu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.form.MenuSaveForm;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.LocalDateTime;
import java.util.List;
import sm.system.util.EnabledCommandUtil;

/**
 * 菜单事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class MenuTxService {
    private final CurrentUserContext currentUserContext;
    private final MenuMapper mapper;

    public Long save(MenuSaveForm form) {
        MenuEntity entity = new MenuEntity();
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "菜单不存在");
            }
        }
        entity.setNumber(form.getNumber());
        entity.setName(form.getName());
        if (form.getLevel() == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单层级不能为空");
        }
        // 菜单层级收敛为两级：分组/页面
        if (!(form.getLevel().equals(MenuLevelEnum.CATEGORY) || form.getLevel().equals(MenuLevelEnum.PAGE))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单层级只能是分组或页面");
        }
        // 页面层级必须指定权限和路径；分组层级不需要路径和组件，但保留权限用于菜单可见性控制
        if (form.getLevel().equals(MenuLevelEnum.PAGE)) {
            if (form.getPermissionId() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "页面层级菜单必须选择权限");
            }
            if (form.getPath() == null || form.getPath().isBlank()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "页面层级菜单必须填写路径");
            }
            entity.setPath(form.getPath());
            entity.setComponent(form.getComponent());
        } else {
            entity.setPath(null);
            entity.setComponent(null);
        }
        entity.setPermissionId(form.getPermissionId());
        entity.setLevel(form.getLevel());
        entity.setParentId(form.getParentId() != null ? form.getParentId() : 0L);
        entity.setAppId(form.getAppId());
        entity.setIcon(form.getIcon());
        entity.setDescription(form.getDescription());
        entity.setSort(form.getSort() != null ? form.getSort() : 99);
        if (form.getId() == null) {
            entity.setEnabled(true);
        }
        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            // 使用全字段 XML 更新，确保分组菜单可以把 permissionId/path/component 清空为 null。
            entity.setUpdateTime(LocalDateTime.now());
            entity.setUpdateUser(currentUserContext.isLogin() ? currentUserContext.getUserId() : null);
            mapper.updateAllColumns(entity);
        }
        return entity.getId();
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "菜单ID不能为空");
        }
        MenuEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "菜单不存在");
        }
        if (mapper.deleteById(id) != 1) {
            throw new BizException(sm.system.response.ResultEnum.DATA_CONFLICT, "数据已被其他用户删除");
        }
    }

    public void updateEnabled(List<Long> ids, boolean enabled) {
        EnabledCommandUtil.update(mapper, MenuEntity::getId, MenuEntity::getEnabled, ids, enabled, "菜单");
    }
}
