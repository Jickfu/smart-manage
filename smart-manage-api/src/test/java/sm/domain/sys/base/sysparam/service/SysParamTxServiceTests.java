package sm.domain.sys.base.sysparam.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.sysparam.mapper.SysParamMapper;
import sm.domain.sys.base.sysparam.model.form.SysParamSaveForm;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

        assertThrows(BizException.class, () -> new SysParamTxService(mapper).save(form));
    }
}
