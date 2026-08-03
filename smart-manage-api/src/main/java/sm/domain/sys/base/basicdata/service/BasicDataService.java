package sm.domain.sys.base.basicdata.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.basicdata.mapper.BasicDataCategoryMapper;
import sm.domain.sys.base.basicdata.mapper.BasicDataItemMapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataCategoryEntity;
import sm.domain.sys.base.basicdata.model.entity.BasicDataItemEntity;
import sm.domain.sys.base.basicdata.model.form.BasicDataCategorySaveForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataDeleteForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataItemSaveForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataListForm;
import sm.domain.sys.base.basicdata.model.vo.BasicDataCategoryVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataItemDetailVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataListVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataOptionVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataTreeVO;
import sm.domain.sys.base.cloud.mapper.CloudMapper;
import sm.domain.sys.base.cloud.model.entity.CloudEntity;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BasicDataService {
    private final BasicDataCategoryMapper categoryMapper;
    private final BasicDataItemMapper itemMapper;
    private final CloudMapper cloudMapper;
    private final BasicDataTxService txService;
    private final BasicDataConverter converter;

    public List<BasicDataTreeVO> categoryTree() {
        List<BasicDataCategoryEntity> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<BasicDataCategoryEntity>().orderByAsc(BasicDataCategoryEntity::getNumber));
        return cloudMapper.selectList(new LambdaQueryWrapper<CloudEntity>()
                        .orderByAsc(CloudEntity::getSeq).orderByAsc(CloudEntity::getId))
                .stream()
                .map(cloud -> new BasicDataTreeVO("cloud:" + cloud.getId(), "cloud", cloud.getId(),
                        cloud.getName(), cloud.getEnabled(), categories.stream()
                        .filter(category -> cloud.getId().equals(category.getCloudId()))
                        .map(category -> new BasicDataTreeVO("category:" + category.getId(), "category",
                                category.getId(), category.getName(), category.getEnabled(), List.of()))
                        .toList()))
                .toList();
    }

    public BasicDataCategoryVO categoryDetail(Long id) {
        BasicDataCategoryEntity category = requireCategory(id);
        BasicDataCategoryVO result = converter.toCategoryVO(category);
        CloudEntity cloud = cloudMapper.selectById(category.getCloudId());
        result.setCloudName(cloud == null ? null : cloud.getName());
        return result;
    }

    public PageData<BasicDataListVO> listPage(BasicDataListForm form) {
        LambdaQueryWrapper<BasicDataItemEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(form.getCategoryId() != null, BasicDataItemEntity::getCategoryId, form.getCategoryId());
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            wrapper.and(query -> query.like(BasicDataItemEntity::getNumber, keyword)
                    .or().like(BasicDataItemEntity::getName, keyword)
                    .or().like(BasicDataItemEntity::getNamePath, keyword));
        }
        wrapper.orderByAsc(BasicDataItemEntity::getNumberPath).orderByAsc(BasicDataItemEntity::getId);
        Page<BasicDataItemEntity> result = itemMapper.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()), wrapper);
        Map<Long, String> categoryNames = new HashMap<>();
        List<BasicDataListVO> records = result.getRecords().stream().map(entity -> {
            BasicDataListVO item = converter.toListVO(entity);
            item.setCategoryName(categoryNames.computeIfAbsent(entity.getCategoryId(),
                    id -> requireCategory(id).getName()));
            return item;
        }).toList();
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    public BasicDataItemDetailVO detail(Long id) {
        BasicDataItemEntity entity = requireItem(id);
        BasicDataItemDetailVO result = converter.toDetailVO(entity);
        result.setCategoryName(requireCategory(entity.getCategoryId()).getName());
        return result;
    }

    public List<BasicDataOptionVO> parentOptions(Long categoryId, Long excludeId) {
        return itemMapper.selectList(new LambdaQueryWrapper<BasicDataItemEntity>()
                        .eq(BasicDataItemEntity::getCategoryId, categoryId)
                        .ne(excludeId != null, BasicDataItemEntity::getId, excludeId)
                        .orderByAsc(BasicDataItemEntity::getNumberPath))
                .stream()
                .filter(item -> excludeId == null || !isDescendantOf(item, excludeId))
                .map(this::toOption)
                .toList();
    }

    @Cached(cacheType = CacheType.LOCAL, name = CacheConstant.BASIC_DATA_OPTIONS,
            key = "#number", expire = 30, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    public List<BasicDataOptionVO> getOptionsByNumber(String number) {
        BasicDataCategoryEntity category = categoryMapper.selectOne(
                new LambdaQueryWrapper<BasicDataCategoryEntity>().eq(BasicDataCategoryEntity::getNumber, number));
        if (category == null || !Boolean.TRUE.equals(category.getEnabled())) {
            throw new BizException(ResultEnum.NOT_FOUND, "基础资料分类不存在或未启用");
        }
        List<BasicDataItemEntity> items = itemMapper.selectList(new LambdaQueryWrapper<BasicDataItemEntity>()
                .eq(BasicDataItemEntity::getCategoryId, category.getId())
                .orderByAsc(BasicDataItemEntity::getNumberPath));
        Map<Long, BasicDataItemEntity> itemMap = new HashMap<>();
        items.forEach(item -> itemMap.put(item.getId(), item));
        // 非叶子只承担分组语义；任一祖先停用时，叶子也不是有效业务选项。
        return items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsLeaf()))
                .filter(item -> isEffectivelyEnabled(item, itemMap))
                .map(this::toOption)
                .toList();
    }

    @BizLog("保存基础资料分类")
    public Long saveCategory(BasicDataCategorySaveForm form) {
        return txService.saveCategory(form);
    }

    @BizLog("删除基础资料分类")
    public void deleteCategory(BasicDataDeleteForm form) {
        txService.deleteCategory(form);
    }

    @BizLog("保存基础资料")
    public Long save(BasicDataItemSaveForm form) {
        return txService.saveItem(form);
    }

    @BizLog("删除基础资料")
    public void delete(BasicDataDeleteForm form) {
        txService.deleteItem(form);
    }

    @BizLog("启用基础资料")
    public void enable(List<Long> ids) {
        txService.updateItemEnabled(ids, true);
    }

    @BizLog("禁用基础资料")
    public void disable(List<Long> ids) {
        txService.updateItemEnabled(ids, false);
    }

    private boolean isDescendantOf(BasicDataItemEntity item, Long ancestorId) {
        Long parentId = item.getParentId();
        while (parentId != null) {
            if (parentId.equals(ancestorId)) return true;
            BasicDataItemEntity parent = itemMapper.selectById(parentId);
            parentId = parent == null ? null : parent.getParentId();
        }
        return false;
    }

    private boolean isEffectivelyEnabled(BasicDataItemEntity item, Map<Long, BasicDataItemEntity> itemMap) {
        BasicDataItemEntity current = item;
        while (current != null) {
            if (!Boolean.TRUE.equals(current.getEnabled())) return false;
            current = current.getParentId() == null ? null : itemMap.get(current.getParentId());
        }
        return true;
    }

    private BasicDataOptionVO toOption(BasicDataItemEntity item) {
        return new BasicDataOptionVO(item.getId(), item.getParentId(), item.getNumber(), item.getName(),
                item.getNamePath(), item.getIsLeaf());
    }

    private BasicDataCategoryEntity requireCategory(Long id) {
        BasicDataCategoryEntity entity = id == null ? null : categoryMapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "基础资料分类不存在");
        return entity;
    }

    private BasicDataItemEntity requireItem(Long id) {
        BasicDataItemEntity entity = id == null ? null : itemMapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "基础资料不存在");
        return entity;
    }
}
