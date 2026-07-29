package sm.domain.sys.base.uiconfig.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.uiconfig.mapper.UiConfigMapper;
import sm.domain.sys.base.uiconfig.model.entity.UiConfigEntity;
import sm.domain.sys.base.uiconfig.model.form.UiConfigSaveForm;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UiConfigTxServiceTests {

    private final UiConfigMapper mapper = mock(UiConfigMapper.class);
    private final UiConfigTxService txService = new UiConfigTxService(mapper);

    @Test
    void rejectsStaleVersion() {
        UiConfigEntity entity = new UiConfigEntity();
        entity.setId(1L);
        entity.setVersion(2);
        when(mapper.selectById(1L)).thenReturn(entity);
        UiConfigSaveForm form = new UiConfigSaveForm();
        form.setId(1L);
        form.setVersion(1);

        assertThrows(BizException.class, () -> txService.save(form, 1L));
    }

    @Test
    void rejectsSecondSingletonRecord() {
        when(mapper.selectCount(null)).thenReturn(1L);

        assertThrows(BizException.class, () -> txService.save(new UiConfigSaveForm(), 1L));
    }
}
