package sm.domain.sys.base.fileconfig.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.fileconfig.mapper.FileConfigMapper;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;
import sm.domain.sys.base.fileconfig.model.form.FileConfigSaveForm;
import sm.system.exception.BizException;
import sm.system.security.crypto.Sm4Cipher;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileConfigTxServiceTests {

    private final FileConfigMapper mapper = mock(FileConfigMapper.class);
    private final FileConfigTxService txService = new FileConfigTxService(mapper, mock(Sm4Cipher.class));

    @Test
    void rejectsStaleVersion() {
        FileConfigEntity entity = new FileConfigEntity();
        entity.setId(1L);
        entity.setVersion(2);
        when(mapper.selectById(1L)).thenReturn(entity);
        FileConfigSaveForm form = new FileConfigSaveForm();
        form.setId(1L);
        form.setVersion(1);

        assertThrows(BizException.class, () -> txService.save(form));
    }

    @Test
    void rejectsSecondSingletonRecord() {
        when(mapper.selectCount(null)).thenReturn(1L);

        assertThrows(BizException.class, () -> txService.save(new FileConfigSaveForm()));
    }
}
