package sm.domain.sys.base.openapi.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.openapi.mapper.OpenApiApplicationMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiCredentialMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiGrantMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiReleaseMapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiApplicationEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiCredentialEntity;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.contract.UserAssignmentReader;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.system.helper.SM4Helper;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenApiRuntimeAccessServiceTests {

    @Test
    void unencryptedApplicationOnlyLoadsSigningSecret() {
        OpenApiCredentialMapper credentialMapper = mock(OpenApiCredentialMapper.class);
        OpenApiApplicationMapper applicationMapper = mock(OpenApiApplicationMapper.class);
        SM4Helper sm4Helper = mock(SM4Helper.class);
        OpenApiCredentialEntity credential = new OpenApiCredentialEntity();
        credential.setApplicationId(10L);
        credential.setKeyId("sm_credential_key");
        credential.setEnabled(true);
        credential.setEncryptionAlgorithm("NONE");
        credential.setSigningSecretCipher("encrypted-signing-secret");
        OpenApiApplicationEntity application = new OpenApiApplicationEntity();
        application.setId(10L);
        application.setNumber("partner");
        application.setEnabled(true);
        application.setEncryptionAlgorithm("NONE");
        application.setIpPolicyMode("DISABLED");
        application.setProxyUserId(20L);
        application.setProxyOrgId(30L);
        byte[] signingSecret = new byte[] {1, 2, 3};
        when(credentialMapper.selectOne(any())).thenReturn(credential);
        when(applicationMapper.selectById(10L)).thenReturn(application);
        when(sm4Helper.decrypt("encrypted-signing-secret"))
                .thenReturn(Base64.getEncoder().encodeToString(signingSecret));
        OpenApiRuntimeAccessService service = new OpenApiRuntimeAccessService(
                credentialMapper, applicationMapper, mock(OpenApiReleaseMapper.class),
                mock(OpenApiGrantMapper.class), mock(UserReferenceReader.class),
                mock(UserAssignmentReader.class), mock(OrgReferenceReader.class), sm4Helper);

        OpenApiRuntimeAccessService.AccessMaterial material =
                service.authenticate("sm_credential_key", "127.0.0.1");

        assertEquals("NONE", material.algorithm());
        assertArrayEquals(signingSecret, material.signingSecret());
        assertNull(material.requestEncryptionKey());
        assertNull(material.responseEncryptionKey());
    }
}
