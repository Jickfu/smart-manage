package sm.domain.sys.base.basicdata.service;

import com.alicp.jetcache.anno.CacheType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.basicdata.mapper.BasicDataCategoryMapper;
import sm.domain.sys.base.basicdata.mapper.BasicDataItemMapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataCategoryEntity;
import sm.domain.sys.base.basicdata.model.entity.BasicDataItemEntity;
import sm.domain.sys.base.basicdata.model.form.BasicDataCategorySaveForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataDeleteForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataItemSaveForm;
import sm.domain.sys.base.basicdata.model.vo.BasicDataOptionVO;
import sm.domain.sys.base.cloud.mapper.CloudMapper;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import sm.system.response.ResultEnum;
import sm.system.util.EnabledCommandUtil;
import sm.system.util.TransactionUtil;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class BasicDataTxService {
    private static final String PATH_SEPARATOR = "/";

    private final BasicDataCategoryMapper categoryMapper;
    private final BasicDataItemMapper itemMapper;
    private final CloudMapper cloudMapper;
    private final CacheHelper cacheHelper;

    public Long saveCategory(BasicDataCategorySaveForm form) {
        normalizeCategory(form);
        if (cloudMapper.selectById(form.getCloudId()) == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "所属云不存在");
        }
        BasicDataCategoryEntity entity;
        String oldNumber = null;
        if (form.getId() == null) {
            entity = new BasicDataCategoryEntity();
            entity.setSystemPreset(false);
        } else {
            entity = requireCategory(form.getId());
            requireVersion(entity.getVersion(), form.getVersion(), "基础资料分类");
            oldNumber = entity.getNumber();
        }
        long duplicateCount = categoryMapper.selectCount(new LambdaQueryWrapper<BasicDataCategoryEntity>()
                .eq(BasicDataCategoryEntity::getNumber, form.getNumber())
                .ne(form.getId() != null, BasicDataCategoryEntity::getId, form.getId()));
        if (duplicateCount > 0) throw new BizException(ResultEnum.DATA_CONFLICT, "基础资料分类编码已存在");

        entity.setCloudId(form.getCloudId());
        entity.setNumber(form.getNumber());
        entity.setName(form.getName());
        entity.setRemark(form.getRemark());
        entity.setEnabled(form.getEnabled() == null || form.getEnabled());
        int affected = form.getId() == null ? categoryMapper.insert(entity) : categoryMapper.updateById(entity);
        if (affected != 1) throw conflict("基础资料分类");
        invalidateAfterCommit(oldNumber, entity.getNumber());
        return entity.getId();
    }

    public void deleteCategory(BasicDataDeleteForm form) {
        BasicDataCategoryEntity entity = requireCategory(form.getId());
        requireVersion(entity.getVersion(), form.getVersion(), "基础资料分类");
        if (Boolean.TRUE.equals(entity.getSystemPreset())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "系统预置分类不能删除");
        }
        if (itemMapper.selectCount(new LambdaQueryWrapper<BasicDataItemEntity>()
                .eq(BasicDataItemEntity::getCategoryId, entity.getId())) > 0) {
            throw new BizException(ResultEnum.PARAM_ERROR, "分类下存在基础资料，不能删除");
        }
        int affected = categoryMapper.delete(new LambdaQueryWrapper<BasicDataCategoryEntity>()
                .eq(BasicDataCategoryEntity::getId, form.getId())
                .eq(BasicDataCategoryEntity::getVersion, form.getVersion()));
        if (affected != 1) throw conflict("基础资料分类");
        invalidateAfterCommit(entity.getNumber());
    }

    public Long saveItem(BasicDataItemSaveForm form) {
        normalizeItem(form);
        BasicDataCategoryEntity category = requireCategory(form.getCategoryId());
        BasicDataItemEntity oldEntity = form.getId() == null ? null : requireItem(form.getId());
        if (oldEntity != null) {
            requireVersion(oldEntity.getVersion(), form.getVersion(), "基础资料");
            if (!oldEntity.getCategoryId().equals(form.getCategoryId())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "基础资料不能跨分类移动");
            }
        }
        BasicDataItemEntity parent = validateParent(form, oldEntity);
        long duplicateCount = itemMapper.selectCount(new LambdaQueryWrapper<BasicDataItemEntity>()
                .eq(BasicDataItemEntity::getCategoryId, form.getCategoryId())
                .eq(BasicDataItemEntity::getNumber, form.getNumber())
                .ne(form.getId() != null, BasicDataItemEntity::getId, form.getId()));
        if (duplicateCount > 0) throw new BizException(ResultEnum.DATA_CONFLICT, "同一分类下基础资料编码不能重复");

        BasicDataItemEntity entity = oldEntity == null ? new BasicDataItemEntity() : oldEntity;
        Long oldParentId = oldEntity == null ? null : oldEntity.getParentId();
        String oldNumberPath = oldEntity == null ? null : oldEntity.getNumberPath();
        String oldNamePath = oldEntity == null ? null : oldEntity.getNamePath();
        int oldLevel = oldEntity == null ? 0 : oldEntity.getLevel();
        entity.setCategoryId(form.getCategoryId());
        entity.setParentId(form.getParentId());
        entity.setNumber(form.getNumber());
        entity.setName(form.getName());
        entity.setRemark(form.getRemark());
        entity.setSort(form.getSort() == null ? 0 : form.getSort());
        entity.setEnabled(form.getEnabled() == null || form.getEnabled());
        entity.setSystemPreset(oldEntity != null && Boolean.TRUE.equals(oldEntity.getSystemPreset()));
        entity.setLevel(parent == null ? 1 : parent.getLevel() + 1);
        entity.setNumberPath(joinPath(parent == null ? null : parent.getNumberPath(), entity.getNumber()));
        entity.setNamePath(joinPath(parent == null ? null : parent.getNamePath(), entity.getName()));
        if (oldEntity == null) entity.setIsLeaf(true);
        int affected = oldEntity == null ? itemMapper.insert(entity) : itemMapper.updateById(entity);
        if (affected != 1) throw conflict("基础资料");

        if (oldEntity != null && (!Objects.equals(oldNumberPath, entity.getNumberPath())
                || !Objects.equals(oldNamePath, entity.getNamePath()) || oldLevel != entity.getLevel())) {
            updateDescendantPaths(entity.getId(), oldNumberPath, oldNamePath, oldLevel, entity);
        }
        refreshLeaf(oldParentId);
        refreshLeaf(entity.getParentId());
        invalidateAfterCommit(category.getNumber());
        return entity.getId();
    }

    public void deleteItem(BasicDataDeleteForm form) {
        BasicDataItemEntity entity = requireItem(form.getId());
        requireVersion(entity.getVersion(), form.getVersion(), "基础资料");
        if (Boolean.TRUE.equals(entity.getSystemPreset())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "系统预置基础资料不能删除");
        }
        if (!Boolean.TRUE.equals(entity.getIsLeaf())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "非叶子基础资料不能删除，请先处理下级资料");
        }
        int affected = itemMapper.delete(new LambdaQueryWrapper<BasicDataItemEntity>()
                .eq(BasicDataItemEntity::getId, form.getId())
                .eq(BasicDataItemEntity::getVersion, form.getVersion()));
        if (affected != 1) throw conflict("基础资料");
        refreshLeaf(entity.getParentId());
        invalidateAfterCommit(requireCategory(entity.getCategoryId()).getNumber());
    }

    public void updateItemEnabled(List<Long> ids, boolean enabled) {
        List<BasicDataItemEntity> items = itemMapper.selectByIds(ids);
        EnabledCommandUtil.update(itemMapper, BasicDataItemEntity::getId, BasicDataItemEntity::getEnabled,
                ids, enabled, "基础资料");
        List<String> categoryNumbers = items.stream().map(BasicDataItemEntity::getCategoryId).distinct()
                .map(this::requireCategory).map(BasicDataCategoryEntity::getNumber).toList();
        TransactionUtil.afterCommit(() -> categoryNumbers.forEach(this::removeOptionsCache));
    }

    private BasicDataItemEntity validateParent(BasicDataItemSaveForm form, BasicDataItemEntity oldEntity) {
        if (form.getParentId() == null) return null;
        if (form.getParentId().equals(form.getId())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "不能选择自身作为上级基础资料");
        }
        BasicDataItemEntity parent = requireItem(form.getParentId());
        if (!parent.getCategoryId().equals(form.getCategoryId())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "上级基础资料必须属于同一分类");
        }
        Long ancestorId = parent.getParentId();
        while (ancestorId != null) {
            if (ancestorId.equals(form.getId())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "不能选择下级资料作为上级");
            }
            BasicDataItemEntity ancestor = requireItem(ancestorId);
            ancestorId = ancestor.getParentId();
        }
        return parent;
    }

    private void updateDescendantPaths(Long rootId, String oldNumberPath, String oldNamePath,
                                       int oldLevel, BasicDataItemEntity root) {
        // 节点改名、改编码或移动后，所有后代的物化路径和级次必须在同一事务内同步。
        List<BasicDataItemEntity> allItems = itemMapper.selectList(new LambdaQueryWrapper<BasicDataItemEntity>()
                .eq(BasicDataItemEntity::getCategoryId, root.getCategoryId()));
        for (BasicDataItemEntity descendant : allItems) {
            if (descendant.getId().equals(rootId) || !isDescendant(descendant, rootId, allItems)) continue;
            descendant.setNumberPath(root.getNumberPath()
                    + descendant.getNumberPath().substring(oldNumberPath.length()));
            descendant.setNamePath(root.getNamePath()
                    + descendant.getNamePath().substring(oldNamePath.length()));
            descendant.setLevel(descendant.getLevel() + root.getLevel() - oldLevel);
            if (itemMapper.updateById(descendant) != 1) throw conflict("下级基础资料");
        }
    }

    private boolean isDescendant(BasicDataItemEntity item, Long rootId, List<BasicDataItemEntity> allItems) {
        Long parentId = item.getParentId();
        while (parentId != null) {
            if (parentId.equals(rootId)) return true;
            Long currentParentId = parentId;
            parentId = allItems.stream().filter(candidate -> candidate.getId().equals(currentParentId))
                    .map(BasicDataItemEntity::getParentId).findFirst().orElse(null);
        }
        return false;
    }

    private void refreshLeaf(Long id) {
        // is_leaf 是有效资料选择的业务字段，不能依赖异步修复或前端传值。
        if (id == null) return;
        BasicDataItemEntity parent = itemMapper.selectById(id);
        if (parent == null) return;
        boolean isLeaf = itemMapper.selectCount(new LambdaQueryWrapper<BasicDataItemEntity>()
                .eq(BasicDataItemEntity::getParentId, id)) == 0;
        if (!Objects.equals(parent.getIsLeaf(), isLeaf)) {
            parent.setIsLeaf(isLeaf);
            if (itemMapper.updateById(parent) != 1) throw conflict("上级基础资料");
        }
    }

    private void normalizeCategory(BasicDataCategorySaveForm form) {
        form.setNumber(form.getNumber().trim());
        form.setName(form.getName().trim());
        form.setRemark(trimToNull(form.getRemark()));
    }

    private void normalizeItem(BasicDataItemSaveForm form) {
        form.setNumber(form.getNumber().trim());
        form.setName(form.getName().trim());
        form.setRemark(trimToNull(form.getRemark()));
        if (form.getNumber().contains(PATH_SEPARATOR) || form.getName().contains(PATH_SEPARATOR)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编码和名称不能包含路径分隔符 /");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String joinPath(String parentPath, String value) {
        return parentPath == null ? value : parentPath + PATH_SEPARATOR + value;
    }

    private void requireVersion(Integer current, Integer requested, String label) {
        if (requested == null) throw new BizException(ResultEnum.PARAM_ERROR, "修改" + label + "时版本号不能为空");
        if (!Objects.equals(current, requested)) throw conflict(label);
    }

    private BizException conflict(String label) {
        return new BizException(ResultEnum.DATA_CONFLICT, label + "已被其他用户修改，请刷新后重试");
    }

    private BasicDataCategoryEntity requireCategory(Long id) {
        BasicDataCategoryEntity entity = categoryMapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "基础资料分类不存在");
        return entity;
    }

    private BasicDataItemEntity requireItem(Long id) {
        BasicDataItemEntity entity = itemMapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "基础资料不存在");
        return entity;
    }

    private void invalidateAfterCommit(String... numbers) {
        TransactionUtil.afterCommit(() -> {
            for (String number : numbers) removeOptionsCache(number);
        });
    }

    private void removeOptionsCache(String number) {
        if (number != null) cacheHelper.<String, List<BasicDataOptionVO>>getCache(
                CacheConstant.BASIC_DATA_OPTIONS, CacheType.LOCAL).remove(number);
    }
}
