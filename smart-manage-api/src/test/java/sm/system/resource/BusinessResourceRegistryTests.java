package sm.system.resource;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;
import sm.domain.sys.base.attachmentconfig.service.AttachmentConfigService;
import sm.system.resource.AttachmentUploadPolicy;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessResourceRegistryTests {
    private final AttachmentConfigService attachmentConfigService = mock(AttachmentConfigService.class);

    @Test
    void unknownResourceTypeIsDenied() {
        BusinessResourceRegistry registry = new BusinessResourceRegistry(List.of(), attachmentConfigService);

        assertThrows(BizException.class, () -> registry.requireRegistered("unknown.resource"));
    }

    @Test
    void registeredPolicyReceivesExactResourceAndAction() {
        BusinessResourceAccessPolicy policy = mock(BusinessResourceAccessPolicy.class);
        BusinessResourceRegistration registration = registration("sys.base.ui-config", policy);
        BusinessResourceRegistry registry = new BusinessResourceRegistry(List.of(registration), attachmentConfigService);

        registry.requireAllowed("sys.base.ui-config", "100", BusinessResourceAction.READ);

        verify(policy).requireAllowed("100", BusinessResourceAction.READ);
    }

    @Test
    void duplicateResourceTypeFailsAtStartup() {
        BusinessResourceAccessPolicy firstPolicy = mock(BusinessResourceAccessPolicy.class);
        BusinessResourceAccessPolicy secondPolicy = mock(BusinessResourceAccessPolicy.class);

        assertThrows(IllegalStateException.class, () -> new BusinessResourceRegistry(List.of(
                registration("sys.base.ui-config", firstPolicy),
                registration("sys.base.ui-config", secondPolicy)), attachmentConfigService));
    }

    @Test
    void invalidResourceTypeFailsAtStartup() {
        assertThrows(IllegalStateException.class, () -> new BusinessResourceRegistry(List.of(
                registration("SYS_UI_CONFIG", mock(BusinessResourceAccessPolicy.class))), attachmentConfigService));
    }

    @Test
    void uploadUsesGlobalLimitInsteadOfRegistrationSpecificLimit() {
        BusinessResourceRegistration registration = registration("sys.base.ui-config", mock(BusinessResourceAccessPolicy.class));
        when(attachmentConfigService.uploadPolicy()).thenReturn(new AttachmentUploadPolicy(
                3L, List.of("txt"), List.of("text/plain"), 24));
        BusinessResourceRegistry registry = new BusinessResourceRegistry(List.of(registration), attachmentConfigService);
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "test".getBytes());

        assertThrows(BizException.class, () -> registry.validateUpload("sys.base.ui-config", file));
    }

    @Test
    void configuredTypeIsDetectedWithoutHardcodedExtensionBranch() throws Exception {
        BusinessResourceRegistration registration = registration("sys.base.ui-config", mock(BusinessResourceAccessPolicy.class));
        when(attachmentConfigService.uploadPolicy()).thenReturn(new AttachmentUploadPolicy(
                1024L, List.of("zip"), List.of("application/zip"), 24));
        BusinessResourceRegistry registry = new BusinessResourceRegistry(List.of(registration), attachmentConfigService);
        MockMultipartFile file = new MockMultipartFile(
                "file", "archive.zip", "application/zip", zipBytes());

        assertDoesNotThrow(() -> registry.validateUpload("sys.base.ui-config", file));
    }

    private byte[] zipBytes() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutput = new ZipOutputStream(output)) {
            zipOutput.putNextEntry(new ZipEntry("readme.md"));
            zipOutput.write("content".getBytes());
            zipOutput.closeEntry();
        }
        return output.toByteArray();
    }

    private BusinessResourceRegistration registration(String resourceType, BusinessResourceAccessPolicy policy) {
        return new BusinessResourceRegistration() {
            @Override
            public String resourceType() {
                return resourceType;
            }

            @Override
            public BusinessResourceAccessPolicy accessPolicy() {
                return policy;
            }
        };
    }
}
