package sm.domain.sys.base.basicdata.service;

import com.alicp.jetcache.Cache;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.basicdata.mapper.BasicDataCategoryMapper;
import sm.domain.sys.base.basicdata.mapper.BasicDataItemMapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataCategoryEntity;
import sm.domain.sys.base.basicdata.model.BasicDataNumberMode;
import sm.domain.sys.base.numberrule.service.NumberGeneratorAccessor;
import sm.domain.sys.base.basicdata.model.entity.BasicDataItemEntity;
import sm.domain.sys.base.basicdata.model.form.BasicDataDeleteForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataItemSaveForm;
import sm.domain.sys.base.domain.mapper.DomainMapper;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicDataTxServiceTests {
    private final BasicDataCategoryMapper categoryMapper = mock(BasicDataCategoryMapper.class);
    private final BasicDataItemMapper itemMapper = mock(BasicDataItemMapper.class);
    private final CacheHelper cacheHelper = mock(CacheHelper.class);
    private final NumberGeneratorAccessor numberGeneratorAccessor = mock(NumberGeneratorAccessor.class);
    private final BasicDataTxService txService = new BasicDataTxService(
            categoryMapper, itemMapper,
            new sm.domain.sys.base.domain.service.DomainReferenceService(mock(DomainMapper.class)), cacheHelper,
            numberGeneratorAccessor);

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "category-test"),
                BasicDataCategoryEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "item-test"),
                BasicDataItemEntity.class);
    }

    @Test
    void deletingNonLeafItemIsRejected() {
        BasicDataItemEntity item = item(10L, null, false);
        item.setVersion(2);
        when(itemMapper.selectById(10L)).thenReturn(item);
        BasicDataDeleteForm form = new BasicDataDeleteForm();
        form.setId(10L);
        form.setVersion(2);

        assertThrows(BizException.class, () -> txService.deleteItem(form));

        verify(itemMapper, never()).delete(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void creatingChildMarksParentAsNonLeaf() {
        BasicDataCategoryEntity category = new BasicDataCategoryEntity();
        category.setId(1L);
        category.setNumber("industry");
        category.setNumberMode(BasicDataNumberMode.AUTO_DEFAULT.name());
        BasicDataItemEntity parent = item(10L, null, true);
        parent.setLevel(1);
        parent.setNumberPath("A");
        parent.setNamePath("农业");
        when(categoryMapper.selectById(1L)).thenReturn(category);
        when(itemMapper.selectById(10L)).thenReturn(parent);
        when(itemMapper.selectCount(any())).thenReturn(0L, 1L);
        when(itemMapper.insert(isA(BasicDataItemEntity.class))).thenAnswer(invocation -> {
            BasicDataItemEntity inserted = invocation.getArgument(0);
            inserted.setId(11L);
            return 1;
        });
        when(itemMapper.updateById(isA(BasicDataItemEntity.class))).thenReturn(1);
        when(cacheHelper.getCache(any(), any(), anyLong())).thenReturn(mock(Cache.class));

        txService.saveItem(saveForm(10L));

        assertFalse(parent.getIsLeaf());
        verify(itemMapper).updateById(parent);
    }

    @Test
    @SuppressWarnings("unchecked")
    void autoDefaultGeneratesNumberOnlyWhenNewItemLeavesItBlank() {
        BasicDataCategoryEntity category = new BasicDataCategoryEntity();
        category.setId(1L);
        category.setNumber("industry");
        category.setNumberMode(BasicDataNumberMode.AUTO_DEFAULT.name());
        category.setNumberRuleKey("sys/base/basic-data-item");
        when(categoryMapper.selectById(1L)).thenReturn(category);
        when(numberGeneratorAccessor.nextNumber(any(), any(), any())).thenReturn("BD-0001");
        when(itemMapper.selectCount(any())).thenReturn(0L);
        when(itemMapper.insert(isA(BasicDataItemEntity.class))).thenAnswer(invocation -> {
            BasicDataItemEntity inserted = invocation.getArgument(0);
            inserted.setId(12L);
            return 1;
        });
        when(cacheHelper.getCache(any(), any(), anyLong())).thenReturn(mock(Cache.class));
        BasicDataItemSaveForm form = saveForm(null);
        form.setNumber(null);

        assertEquals(12L, txService.saveItem(form));

        verify(numberGeneratorAccessor).nextNumber(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void savingDisabledItemKeepsEnabledState() {
        BasicDataCategoryEntity category = new BasicDataCategoryEntity();
        category.setId(1L);
        category.setNumber("industry");
        category.setNumberMode(BasicDataNumberMode.MANUAL.name());
        BasicDataItemEntity existing = item(10L, null, true);
        existing.setEnabled(false);
        existing.setVersion(2);
        existing.setNumber("A01");
        existing.setName("原名称");
        existing.setNumberPath("A01");
        existing.setNamePath("原名称");
        existing.setLevel(1);
        when(categoryMapper.selectById(1L)).thenReturn(category);
        when(itemMapper.selectById(10L)).thenReturn(existing);
        when(itemMapper.selectCount(any())).thenReturn(0L);
        when(itemMapper.updateById(existing)).thenReturn(1);
        when(cacheHelper.getCache(any(), any(), anyLong())).thenReturn(mock(Cache.class));
        BasicDataItemSaveForm form = saveForm(null);
        form.setId(10L);
        form.setVersion(2);

        txService.saveItem(form);

        assertFalse(existing.getEnabled());
    }

    @Test
    @SuppressWarnings("unchecked")
    void creatingItemDefaultsToEnabled() {
        BasicDataCategoryEntity category = new BasicDataCategoryEntity();
        category.setId(1L);
        category.setNumber("industry");
        category.setNumberMode(BasicDataNumberMode.MANUAL.name());
        when(categoryMapper.selectById(1L)).thenReturn(category);
        when(itemMapper.selectCount(any())).thenReturn(0L);
        when(itemMapper.insert(isA(BasicDataItemEntity.class))).thenAnswer(invocation -> {
            BasicDataItemEntity inserted = invocation.getArgument(0);
            inserted.setId(13L);
            assertTrue(inserted.getEnabled());
            return 1;
        });
        when(cacheHelper.getCache(any(), any(), anyLong())).thenReturn(mock(Cache.class));

        txService.saveItem(saveForm(null));

        verify(itemMapper).insert(isA(BasicDataItemEntity.class));
    }

    private BasicDataItemSaveForm saveForm(Long parentId) {
        BasicDataItemSaveForm form = new BasicDataItemSaveForm();
        form.setCategoryId(1L);
        form.setParentId(parentId);
        form.setNumber("A01");
        form.setName("谷物种植");
        return form;
    }

    private BasicDataItemEntity item(Long id, Long parentId, boolean isLeaf) {
        BasicDataItemEntity item = new BasicDataItemEntity();
        item.setId(id);
        item.setCategoryId(1L);
        item.setParentId(parentId);
        item.setIsLeaf(isLeaf);
        item.setEnabled(true);
        item.setSystemPreset(false);
        return item;
    }
}
