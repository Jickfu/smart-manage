package sm.domain.sys.base.fileconfig.service;

import sm.domain.sys.base.fileconfig.converter.FileConfigConverterImpl;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.fileconfig.mapper.FileConfigMapper;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;
import sm.system.helper.SM4Helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;

class FileConfigServiceTests {

    @Test
    void managementDetailOnlyReportsWhetherPasswordIsConfigured() {
        FileConfigMapper mapper = mock(FileConfigMapper.class);
        FileConfigEntity entity = new FileConfigEntity();
        entity.setId(1L);
        entity.setFtpPasswordCipher("cipher-text");
        when(mapper.selectList(null)).thenReturn(List.of(entity));

        FileConfigService service = new FileConfigService(
                mapper, mock(FileConfigTxService.class), mock(SM4Helper.class),
                new FileConfigConverterImpl());

        assertTrue(service.singleton().getFtpPasswordConfigured());

        entity.setFtpPasswordCipher(null);
        assertFalse(service.singleton().getFtpPasswordConfigured());
    }
}
