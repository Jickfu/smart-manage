package sm.domain.sys.base.feature.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.domain.sys.base.feature.model.form.FeatureSaveForm;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeatureTxServiceTests {
    private final FeatureMapper mapper = mock(FeatureMapper.class);
    private final FeatureTxService service = new FeatureTxService(mapper);

    @Test
    void staleVersionCannotOverwriteFeatureConfiguration() {
        FeatureEntity entity = existingFeature();
        when(mapper.selectById(entity.getId())).thenReturn(entity);
        FeatureSaveForm form = validForm();
        form.setVersion(1);

        assertThrows(BizException.class, () -> service.save(form));
        verify(mapper, never()).updateById(any(FeatureEntity.class));
    }

    @Test
    void saveOnlyChangesOperationalFields() {
        FeatureEntity entity = existingFeature();
        when(mapper.selectById(entity.getId())).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);
        FeatureSaveForm form = validForm();
        form.setCustomName("  用户中心  ");

        service.save(form);

        assertEquals("sys/base/user", entity.getFeatureKey());
        assertEquals(31L, entity.getAppId());
        assertEquals("用户管理", entity.getDefaultName());
        assertEquals("用户中心", entity.getCustomName());
    }

    private FeatureEntity existingFeature() {
        FeatureEntity entity = new FeatureEntity();
        entity.setId(100L);
        entity.setVersion(2);
        entity.setFeatureKey("sys/base/user");
        entity.setAppId(31L);
        entity.setDefaultName("用户管理");
        return entity;
    }

    private FeatureSaveForm validForm() {
        FeatureSaveForm form = new FeatureSaveForm();
        form.setId(100L);
        form.setVersion(2);
        form.setVisible(true);
        return form;
    }
}
