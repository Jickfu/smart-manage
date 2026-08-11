package sm.domain.sys.base.org.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.OrgType;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.org.model.form.OrgSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class OrgTxService {
    private static final String PATH_SEPARATOR = "/";
    private final OrgMapper mapper;

    public Long save(OrgSaveForm form) {
        normalize(form);
        OrgEntity oldEntity = form.getId() == null ? null : requireOrg(form.getId());
        if (oldEntity != null) {
            requireVersion(oldEntity, form.getVersion());
            if (Boolean.TRUE.equals(oldEntity.getArchived())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "已封存组织不能编辑");
            }
        }
        if (mapper.selectCount(new LambdaQueryWrapper<OrgEntity>()
                .eq(OrgEntity::getNumber, form.getNumber())
                .ne(form.getId() != null, OrgEntity::getId, form.getId())) > 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "组织编码已存在");
        }
        OrgEntity parent = validateParent(form);
        validateType(form.getOrgType(), parent);

        OrgEntity entity = oldEntity == null ? new OrgEntity() : oldEntity;
        String oldNumberPath = oldEntity == null ? null : oldEntity.getNumberPath();
        String oldNamePath = oldEntity == null ? null : oldEntity.getNamePath();
        entity.setNumber(form.getNumber());
        entity.setName(form.getName());
        entity.setParentId(form.getParentId());
        entity.setOrgType(form.getOrgType());
        entity.setSort(form.getSort() == null ? 99 : form.getSort());
        entity.setDescription(trimToNull(form.getDescription()));
        entity.setNumberPath(joinPath(parent == null ? null : parent.getNumberPath(), entity.getNumber()));
        entity.setNamePath(joinPath(parent == null ? null : parent.getNamePath(), entity.getName()));
        if (oldEntity == null) {
            entity.setEnabled(true);
            entity.setArchived(false);
            if (mapper.insert(entity) != 1) throw persistenceError("新增组织失败");
        } else if (mapper.updateById(entity) != 1) {
            throw conflict();
        }
        if (oldEntity != null && (!Objects.equals(oldNumberPath, entity.getNumberPath())
                || !Objects.equals(oldNamePath, entity.getNamePath()))) {
            updateDescendantPaths(entity.getId(), oldNumberPath, oldNamePath, entity);
        }
        return entity.getId();
    }

    public void updateEnabled(List<Long> ids, boolean enabled) {
        List<OrgEntity> organizations = requireOrganizations(ids);
        for (OrgEntity organization : organizations) {
            if (enabled && Boolean.TRUE.equals(organization.getArchived())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "已封存组织不能启用");
            }
        }
        for (OrgEntity organization : organizations) {
            if (Objects.equals(organization.getEnabled(), enabled)) continue;
            organization.setEnabled(enabled);
            if (mapper.updateById(organization) != 1) throw conflict();
        }
    }

    public void archive(List<Long> ids) {
        List<OrgEntity> organizations = requireOrganizations(ids);
        Set<Long> selectedIds = new HashSet<>(ids);
        List<OrgEntity> allOrganizations = mapper.selectList(null);
        Map<Long, OrgEntity> organizationById = indexById(allOrganizations);
        for (OrgEntity organization : organizations) {
            if (Boolean.TRUE.equals(organization.getArchived())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "选择的组织中包含已封存组织");
            }
            for (OrgEntity candidate : allOrganizations) {
                if (!Boolean.TRUE.equals(candidate.getArchived())
                        && isDescendant(candidate, organization.getId(), organizationById)
                        && !selectedIds.contains(candidate.getId())) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "组织存在未封存下级，请先处理下级组织");
                }
            }
        }
        LocalDateTime archivedAt = LocalDateTime.now();
        // 同批次允许同时封存父子组织，但仍按逐条乐观锁更新保证并发冲突可见。
        for (OrgEntity organization : organizations) {
            organization.setEnabled(false);
            organization.setArchived(true);
            organization.setArchivedAt(archivedAt);
            if (mapper.updateById(organization) != 1) throw conflict();
        }
    }

    public void unarchive(List<Long> ids) {
        List<OrgEntity> organizations = requireOrganizations(ids);
        Set<Long> selectedIds = new HashSet<>(ids);
        for (OrgEntity organization : organizations) {
            if (!Boolean.TRUE.equals(organization.getArchived())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "选择的组织中包含未封存组织");
            }
            if (organization.getParentId() != null) {
                OrgEntity parent = requireOrg(organization.getParentId());
                if (Boolean.TRUE.equals(parent.getArchived()) && !selectedIds.contains(parent.getId())) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "上级组织仍处于封存状态，不能解封");
                }
            }
        }
        for (OrgEntity organization : organizations) {
            organization.setEnabled(false);
            organization.setArchived(false);
            organization.setArchivedAt(null);
            if (mapper.updateById(organization) != 1) throw conflict();
        }
    }

    private OrgEntity validateParent(OrgSaveForm form) {
        if (form.getParentId() == null) return null;
        if (form.getParentId().equals(form.getId())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "不能选择自身作为上级组织");
        }
        OrgEntity parent = requireOrg(form.getParentId());
        if (Boolean.TRUE.equals(parent.getArchived())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "已封存组织不能作为上级组织");
        }
        Long ancestorId = parent.getParentId();
        while (ancestorId != null) {
            if (ancestorId.equals(form.getId())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "不能选择下级组织作为上级组织");
            }
            ancestorId = requireOrg(ancestorId).getParentId();
        }
        return parent;
    }

    private void validateType(OrgType type, OrgEntity parent) {
        if (type == OrgType.GROUP && parent != null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "集团只能作为顶级组织");
        }
        if (type == OrgType.COMPANY && parent != null && parent.getOrgType() == OrgType.DEPARTMENT) {
            throw new BizException(ResultEnum.PARAM_ERROR, "公司不能设置在部门下");
        }
    }

    private void updateDescendantPaths(Long rootId, String oldNumberPath, String oldNamePath, OrgEntity root) {
        List<OrgEntity> allOrganizations = mapper.selectList(null);
        Map<Long, OrgEntity> organizationById = indexById(allOrganizations);
        for (OrgEntity descendant : allOrganizations) {
            if (!descendant.getId().equals(rootId) && isDescendant(descendant, rootId, organizationById)) {
                descendant.setNumberPath(root.getNumberPath() + descendant.getNumberPath().substring(oldNumberPath.length()));
                descendant.setNamePath(root.getNamePath() + descendant.getNamePath().substring(oldNamePath.length()));
                if (mapper.updateById(descendant) != 1) throw conflict();
            }
        }
    }

    private boolean isDescendant(OrgEntity organization, Long ancestorId, Map<Long, OrgEntity> organizationById) {
        Long parentId = organization.getParentId();
        while (parentId != null) {
            if (parentId.equals(ancestorId)) return true;
            OrgEntity parent = organizationById.get(parentId);
            parentId = parent == null ? null : parent.getParentId();
        }
        return false;
    }

    private List<OrgEntity> requireOrganizations(List<Long> ids) {
        List<OrgEntity> organizations = mapper.selectByIds(ids);
        if (organizations.size() != new HashSet<>(ids).size()) {
            throw new BizException(ResultEnum.NOT_FOUND, "部分组织不存在");
        }
        return organizations;
    }

    private Map<Long, OrgEntity> indexById(List<OrgEntity> organizations) {
        Map<Long, OrgEntity> organizationById = new HashMap<>();
        for (OrgEntity organization : organizations) organizationById.put(organization.getId(), organization);
        return organizationById;
    }

    private OrgEntity requireOrg(Long id) {
        OrgEntity entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "组织不存在");
        return entity;
    }

    private void requireVersion(OrgEntity entity, Integer version) {
        if (version == null) throw new BizException(ResultEnum.PARAM_ERROR, "修改组织时版本号不能为空");
        if (!Objects.equals(entity.getVersion(), version)) throw conflict();
    }

    private void normalize(OrgSaveForm form) {
        form.setNumber(form.getNumber().trim());
        form.setName(form.getName().trim());
        if (form.getNumber().contains(PATH_SEPARATOR) || form.getName().contains(PATH_SEPARATOR)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编码和名称不能包含路径分隔符 /");
        }
    }

    private String joinPath(String parentPath, String value) {
        return parentPath == null ? value : parentPath + PATH_SEPARATOR + value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BizException conflict() {
        return new BizException(ResultEnum.DATA_CONFLICT, "组织已被其他用户修改，请刷新后重试");
    }

    private BizException persistenceError(String message) {
        return new BizException(ResultEnum.PERSISTENCE_ERROR, message);
    }
}
