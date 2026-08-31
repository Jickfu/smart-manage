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
import sm.domain.sys.base.domain.service.DomainReferenceService;
import sm.domain.sys.base.domain.model.entity.DomainEntity;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListQueryUtil;
import sm.domain.sys.base.numberrule.model.vo.NumberRuleOptionVO;
import sm.domain.sys.base.numberrule.contract.NumberRuleKeys;
import sm.domain.sys.base.numberrule.service.NumberRuleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BasicDataService {
    private static final Map<String, ListQueryUtil.Field<BasicDataItemEntity>> LIST_FIELDS = Map.ofEntries(
            Map.entry("number", ListQueryUtil.string(BasicDataItemEntity::getNumber, false)),
            Map.entry("name", ListQueryUtil.string(BasicDataItemEntity::getName, false)),
            Map.entry("namePath", ListQueryUtil.string(BasicDataItemEntity::getNamePath, false)),
            Map.entry("numberPath", ListQueryUtil.string(BasicDataItemEntity::getNumberPath, false)),
            Map.entry("level", ListQueryUtil.number(BasicDataItemEntity::getLevel, false)),
            Map.entry("isLeaf", ListQueryUtil.bool(BasicDataItemEntity::getIsLeaf, false)),
            Map.entry("enabled", ListQueryUtil.bool(BasicDataItemEntity::getEnabled, false)),
            Map.entry("systemPreset", ListQueryUtil.bool(BasicDataItemEntity::getSystemPreset, false)),
            Map.entry("description", ListQueryUtil.string(BasicDataItemEntity::getDescription, false)));
    private final BasicDataCategoryMapper categoryMapper;
    private final BasicDataItemMapper itemMapper;
    private final DomainReferenceService domainReferenceService;
    private final BasicDataTxService txService;
    private final BasicDataConverter converter;
    private final NumberRuleService numberRuleService;

    public List<NumberRuleOptionVO> numberRuleOptions() {
        return numberRuleService.options("CATEGORY", NumberRuleKeys.BASIC_DATA_ITEM_REFERENCE);
    }

    public List<BasicDataTreeVO> categoryTree() {
        List<BasicDataCategoryEntity> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<BasicDataCategoryEntity>().orderByAsc(BasicDataCategoryEntity::getNumber));
        Map<Long, List<BasicDataCategoryEntity>> categoriesByDomain = new LinkedHashMap<>();
        for (BasicDataCategoryEntity category : categories) {
            categoriesByDomain.computeIfAbsent(category.getDomainId(), ignored -> new ArrayList<>()).add(category);
        }
        List<DomainEntity> domains = domainReferenceService.findAll().stream()
                .sorted(java.util.Comparator.comparing(DomainEntity::getSeq).thenComparing(DomainEntity::getId))
                .toList();
        List<BasicDataTreeVO> tree = new ArrayList<>(domains.size());
        for (DomainEntity domain : domains) {
            List<BasicDataTreeVO> children = new ArrayList<>();
            for (BasicDataCategoryEntity category : categoriesByDomain.getOrDefault(domain.getId(), List.of())) {
                children.add(new BasicDataTreeVO("category:" + category.getId(), "category",
                        category.getId(), category.getName(), category.getEnabled(), List.of()));
            }
            tree.add(new BasicDataTreeVO("domain:" + domain.getId(), "domain", domain.getId(),
                    domain.getName(), domain.getEnabled(), children));
        }
        return tree;
    }

    public BasicDataCategoryVO categoryDetail(Long id) {
        BasicDataCategoryEntity category = requireCategory(id);
        BasicDataCategoryVO result = converter.toCategoryVO(category);
        DomainEntity domain = domainReferenceService.require(category.getDomainId());
        result.setDomainName(domain.getName());
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
        ListQueryUtil.apply(wrapper, form, LIST_FIELDS);
        wrapper.orderByAsc(BasicDataItemEntity::getNumberPath).orderByAsc(BasicDataItemEntity::getId);
        Page<BasicDataItemEntity> result = itemMapper.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()), wrapper);
        Set<Long> categoryIds = new HashSet<>();
        for (BasicDataItemEntity entity : result.getRecords()) {
            categoryIds.add(entity.getCategoryId());
        }
        Map<Long, String> categoryNames = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            for (BasicDataCategoryEntity category : categoryMapper.selectByIds(categoryIds)) {
                categoryNames.put(category.getId(), category.getName());
            }
        }
        List<BasicDataListVO> records = new ArrayList<>(result.getRecords().size());
        for (BasicDataItemEntity entity : result.getRecords()) {
            BasicDataListVO item = converter.toListVO(entity);
            item.setCategoryName(categoryNames.get(entity.getCategoryId()));
            records.add(item);
        }
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    public BasicDataItemDetailVO detail(Long id) {
        BasicDataItemEntity entity = requireItem(id);
        BasicDataItemDetailVO result = converter.toDetailVO(entity);
        BasicDataCategoryEntity category = requireCategory(entity.getCategoryId());
        result.setCategory(new ReferenceVO(category.getId(), category.getNumber(), category.getName()));
        if (entity.getParentId() != null) {
            BasicDataItemEntity parent = requireItem(entity.getParentId());
            result.setParent(new ReferenceVO(parent.getId(), parent.getNumber(), parent.getName()));
        }
        return result;
    }

    public List<BasicDataOptionVO> parentOptions(Long categoryId, Long excludeId) {
        List<BasicDataItemEntity> items = itemMapper.selectList(new LambdaQueryWrapper<BasicDataItemEntity>()
                .eq(BasicDataItemEntity::getCategoryId, categoryId)
                .orderByAsc(BasicDataItemEntity::getNumberPath));
        Map<Long, BasicDataItemEntity> itemById = new HashMap<>();
        for (BasicDataItemEntity item : items) {
            itemById.put(item.getId(), item);
        }
        List<BasicDataOptionVO> options = new ArrayList<>();
        for (BasicDataItemEntity item : items) {
            if (excludeId != null && (excludeId.equals(item.getId())
                    || isDescendantOf(item, excludeId, itemById))) {
                continue;
            }
            options.add(toOption(item));
        }
        return options;
    }

    @Cached(cacheType = CacheType.REMOTE, name = BaseCacheName.BASIC_DATA_OPTIONS,
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

    private boolean isDescendantOf(BasicDataItemEntity item, Long ancestorId,
                                   Map<Long, BasicDataItemEntity> itemById) {
        Long parentId = item.getParentId();
        while (parentId != null) {
            if (parentId.equals(ancestorId)) return true;
            BasicDataItemEntity parent = itemById.get(parentId);
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
