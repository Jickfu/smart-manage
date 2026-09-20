package sm.domain.sys.base.sysparam.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.sysparam.mapper.SysParamMapper;
import sm.domain.sys.base.sysparam.model.form.SysParamSaveForm;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SysParamTxServiceTests {

    @Test
    void pollingIntervalValidationUsesPersistedSystemNumber() {
        SysParamMapper mapper = mock(SysParamMapper.class);
        var entity = new sm.domain.sys.base.sysparam.model.entity.SysParamEntity();
        entity.setId(1L);
        entity.setNumber("INBOX_POLL_INTERVAL_SECONDS");
        entity.setVersion(0);
        entity.setIsSystem(true);
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);
        var service = new SysParamTxService(mapper, new sm.domain.sys.base.feature.service.FeatureReferenceService(mock(FeatureMapper.class)));
        var form = new SysParamSaveForm();
        form.setId(1L);
        form.setVersion(0);
        form.setNumber("OTHER");
        for (String value : new String[]{"", "-1", "1", "9", "10.5", "2147484", "invalid"}) {
            form.setValue(value);
            assertThrows(BizException.class, () -> service.save(form));
        }
        for (String value : new String[]{"0", "10", "60", "2147483"}) {
            form.setValue(value);
            assertEquals(1L, service.save(form));
        }
    }

    @Test
    void rejectsDuplicateNumberBeforePersistence() {
        SysParamMapper mapper = mock(SysParamMapper.class);
        when(mapper.selectCount(any())).thenReturn(1L);
        SysParamSaveForm form = new SysParamSaveForm();
        form.setNumber("DUPLICATE");
        form.setName("重复参数");

        assertThrows(BizException.class, () -> new SysParamTxService(mapper,
                new sm.domain.sys.base.feature.service.FeatureReferenceService(mock(FeatureMapper.class)))
                .save(form));
    }

    @Test
    void rejectsUnknownFeatureBeforePersistence() {
        SysParamMapper mapper = mock(SysParamMapper.class);
        FeatureMapper featureMapper = mock(FeatureMapper.class);
        when(mapper.selectCount(any())).thenReturn(0L);
        SysParamSaveForm form = new SysParamSaveForm();
        form.setNumber("CUSTOM_PARAM");
        form.setName("自定义参数");
        form.setFeatureId(99L);

        BizException exception = assertThrows(BizException.class,
                () -> new SysParamTxService(mapper,
                        new sm.domain.sys.base.feature.service.FeatureReferenceService(featureMapper)).save(form));

        assertEquals(ResultEnum.NOT_FOUND.getCode(), exception.getCode());
    }
}
