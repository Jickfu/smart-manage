package sm.domain.sys.base.fileconfig.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.fileconfig.mapper.FileConfigMapper;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;
import sm.system.helper.SM4Helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileConfigServiceTests {

    @Test
    void managementDetailOnlyReportsWhetherPasswordIsConfigured() {
        FileConfigMapper mapper = mock(FileConfigMapper.class);
        FileConfigEntity entity = new FileConfigEntity();
        entity.setId(1L);
        entity.setFtpPasswordCipher("cipher-text");
        when(mapper.selectById(1L)).thenReturn(entity);

        FileConfigService service = new FileConfigService(
                mapper, mock(FileConfigTxService.class), mock(SM4Helper.class),
                new FileConfigConverterImpl());

        assertTrue(service.detail(1L).getFtpPasswordConfigured());

        entity.setFtpPasswordCipher(null);
        assertFalse(service.detail(1L).getFtpPasswordConfigured());
    }
}
