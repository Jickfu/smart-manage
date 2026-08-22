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
    void rejectsDuplicateNumberBeforePersistence() {
        SysParamMapper mapper = mock(SysParamMapper.class);
        when(mapper.selectCount(any())).thenReturn(1L);
        SysParamSaveForm form = new SysParamSaveForm();
        form.setNumber("DUPLICATE");
        form.setName("重复参数");

        assertThrows(BizException.class, () -> new SysParamTxService(mapper, mock(FeatureMapper.class)).save(form));
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
                () -> new SysParamTxService(mapper, featureMapper).save(form));

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), exception.getCode());
    }
}
