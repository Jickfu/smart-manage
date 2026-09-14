package sm.domain.sys.base.fileconfig.service;

import sm.domain.sys.base.fileconfig.converter.FileConfigConverterImpl;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.fileconfig.mapper.FileConfigMapper;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;
import sm.system.security.crypto.Sm4Cipher;

import sm.system.exception.BizException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                mapper, mock(FileConfigTxService.class), mock(Sm4Cipher.class),
                new FileConfigConverterImpl());

        assertTrue(service.singleton().getFtpPasswordConfigured());

        entity.setFtpPasswordCipher(null);
        assertFalse(service.singleton().getFtpPasswordConfigured());
    }
    @Test
    void missingConfigurationAllowsManagementButRejectsStorage() {
        FileConfigMapper mapper = mock(FileConfigMapper.class);
        when(mapper.selectList(null)).thenReturn(List.of());
        FileConfigService service = new FileConfigService(
                mapper, mock(FileConfigTxService.class), mock(Sm4Cipher.class),
                new FileConfigConverterImpl());

        assertNull(service.singleton().getId());
        assertNull(service.singleton().getLocalDir());
        assertEquals("LOCAL", service.singleton().getStorageType());
        assertThrows(BizException.class, service::getFileStorageConfig);
    }

    @Test
    void localStorageUsesDatabaseDirectoryAndRejectsMissingDirectory() {
        FileConfigMapper mapper = mock(FileConfigMapper.class);
        FileConfigEntity entity = new FileConfigEntity();
        entity.setStorageType("LOCAL");
        entity.setLocalDir("./custom-files/");
        when(mapper.selectList(null)).thenReturn(List.of(entity));
        FileConfigService service = new FileConfigService(
                mapper, mock(FileConfigTxService.class), mock(Sm4Cipher.class),
                new FileConfigConverterImpl());

        assertEquals("./custom-files/", service.getFileStorageConfig().localDir());
        entity.setLocalDir(null);
        assertThrows(BizException.class, service::getFileStorageConfig);
        entity.setLocalDir("  ");
        assertThrows(BizException.class, service::getFileStorageConfig);
    }
}
