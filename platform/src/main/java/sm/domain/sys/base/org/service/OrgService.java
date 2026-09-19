package sm.domain.sys.base.org.service;

import sm.domain.sys.base.org.converter.OrgConverter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.org.model.form.OrgListForm;
import sm.domain.sys.base.org.model.form.OrgParentListForm;
import sm.domain.sys.base.org.model.form.OrgSaveForm;
import sm.domain.sys.base.org.model.vo.OrgDetailVO;
import sm.domain.sys.base.org.model.vo.OrgListVO;
import sm.domain.sys.base.org.model.vo.OrgOptionVO;
import sm.domain.sys.base.org.model.vo.OrgTreeVO;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListQueryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrgService {
    private static final Map<String, ListQueryUtil.Field<OrgEntity>> LIST_FIELDS = Map.of(
            "number", ListQueryUtil.string(OrgEntity::getNumber, false),
            "name", ListQueryUtil.string(OrgEntity::getName, false),
            "namePath", ListQueryUtil.string(OrgEntity::getNamePath, false),
            "orgType", ListQueryUtil.enumeration(OrgEntity::getOrgType, false),
            "enabled", ListQueryUtil.bool(OrgEntity::getEnabled, false),
            "archived", ListQueryUtil.bool(OrgEntity::getArchived, false),
            "archivedAt", ListQueryUtil.dateTime(OrgEntity::getArchivedAt, false),
            "description", ListQueryUtil.string(OrgEntity::getDescription, false));
    private final OrgMapper mapper;
    private final OrgTxService txService;
    private final OrgConverter converter;

    public PageData<OrgListVO> listPage(OrgListForm form) {
        LambdaQueryWrapper<OrgEntity> query = new LambdaQueryWrapper<>();
        if (!Boolean.TRUE.equals(form.getShowArchived())) {
            query.eq(OrgEntity::getArchived, false);
        }
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            query.and(condition -> condition.like(OrgEntity::getNumber, keyword)
                    .or().like(OrgEntity::getName, keyword)
                    .or().like(OrgEntity::getNamePath, keyword));
        } else if (form.getParentId() != null) {
            if (Boolean.TRUE.equals(form.getIncludeDescendants())) {
                query.in(OrgEntity::getId, findSubtreeIds(form.getParentId()));
            } else {
                query.and(scope -> scope.eq(OrgEntity::getId, form.getParentId())
                        .or().eq(OrgEntity::getParentId, form.getParentId()));
            }
        } else {
            query.isNull(OrgEntity::getParentId);
        }
        ListQueryUtil.apply(query, form, LIST_FIELDS);
        query.orderByAsc(OrgEntity::getSort).orderByAsc(OrgEntity::getNumber).orderByAsc(OrgEntity::getId);
        Page<OrgEntity> page = mapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), query);
        List<OrgListVO> records = page.getRecords().stream().map(converter::toListVO).toList();
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    public PageData<OrgListVO> parentListPage(OrgParentListForm form) {
        LambdaQueryWrapper<OrgEntity> query = new LambdaQueryWrapper<OrgEntity>()
                .eq(OrgEntity::getArchived, false);
        if (form.getExcludedId() != null) {
            requireOrg(form.getExcludedId());
            query.notIn(OrgEntity::getId, findSubtreeIds(form.getExcludedId()));
        }
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            query.and(condition -> condition.like(OrgEntity::getNumber, keyword)
                    .or().like(OrgEntity::getName, keyword)
                    .or().like(OrgEntity::getNamePath, keyword));
        } else if (form.getParentId() != null) {
            query.and(scope -> scope.eq(OrgEntity::getId, form.getParentId())
                    .or().eq(OrgEntity::getParentId, form.getParentId()));
        } else {
            query.isNull(OrgEntity::getParentId);
        }
        query.orderByAsc(OrgEntity::getSort).orderByAsc(OrgEntity::getNumber).orderByAsc(OrgEntity::getId);
        Page<OrgEntity> page = mapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), query);
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(),
                page.getRecords().stream().map(converter::toListVO).toList());
    }

    public OrgDetailVO detail(Long id) {
        OrgEntity entity = requireOrg(id);
        OrgDetailVO detail = converter.toDetailVO(entity);
        if (entity.getParentId() != null) {
            OrgEntity parent = requireOrg(entity.getParentId());
            detail.setParent(new ReferenceVO(parent.getId(), parent.getNumber(), parent.getName()));
        }
        return detail;
    }

    public List<OrgTreeVO> tree(boolean showArchived) {
        List<OrgEntity> organizations = mapper.selectList(new LambdaQueryWrapper<OrgEntity>()
                .eq(!showArchived, OrgEntity::getArchived, false)
                .orderByAsc(OrgEntity::getSort).orderByAsc(OrgEntity::getNumber).orderByAsc(OrgEntity::getId));
        return assembleTree(organizations);
    }

    public List<OrgOptionVO> options() {
        return mapper.selectList(new LambdaQueryWrapper<OrgEntity>()
                        .eq(OrgEntity::getEnabled, true)
                        .eq(OrgEntity::getArchived, false)
                        .orderByAsc(OrgEntity::getSort).orderByAsc(OrgEntity::getNumber).orderByAsc(OrgEntity::getId))
                .stream().map(converter::toOptionVO).toList();
    }

    @BizLog("保存组织")
    public Long save(OrgSaveForm form) {
        return txService.save(form);
    }

    @BizLog("启用组织")
    public void enable(List<Long> ids) {
        txService.updateEnabled(ids, true);
    }

    @BizLog("禁用组织")
    public void disable(List<Long> ids) {
        txService.updateEnabled(ids, false);
    }

    @BizLog("封存组织")
    public void archive(List<Long> ids) {
        txService.archive(ids);
    }

    @BizLog("解封组织")
    public void unarchive(List<Long> ids) {
        txService.unarchive(ids);
    }

    List<OrgTreeVO> assembleTree(List<OrgEntity> organizations) {
        Map<Long, OrgTreeVO> nodeById = new HashMap<>();
        for (OrgEntity organization : organizations) {
            nodeById.put(organization.getId(), converter.toTreeVO(organization));
        }
        List<OrgTreeVO> roots = new ArrayList<>();
        for (OrgEntity organization : organizations) {
            OrgTreeVO node = nodeById.get(organization.getId());
            if (organization.getParentId() == null) {
                roots.add(node);
            } else {
                OrgTreeVO parent = nodeById.get(organization.getParentId());
                if (parent == null) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "组织树存在无效上级");
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private OrgEntity requireOrg(Long id) {
        if (id == null) throw new BizException(ResultEnum.PARAM_ERROR, "组织ID不能为空");
        OrgEntity entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "组织不存在");
        return entity;
    }

    private List<Long> findSubtreeIds(Long rootId) {
        List<OrgEntity> organizations = mapper.selectList(null);
        Map<Long, List<Long>> childIdsByParent = new HashMap<>();
        for (OrgEntity organization : organizations) {
            if (organization.getParentId() != null) {
                childIdsByParent.computeIfAbsent(organization.getParentId(), ignored -> new ArrayList<>())
                        .add(organization.getId());
            }
        }
        List<Long> subtreeIds = new ArrayList<>();
        subtreeIds.add(rootId);
        for (int index = 0; index < subtreeIds.size(); index++) {
            subtreeIds.addAll(childIdsByParent.getOrDefault(subtreeIds.get(index), List.of()));
        }
        return subtreeIds;
    }
}
