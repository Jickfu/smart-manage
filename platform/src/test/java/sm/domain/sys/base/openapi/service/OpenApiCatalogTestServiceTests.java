package sm.domain.sys.base.openapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sm.domain.sys.base.openapi.mapper.OpenApiApplicationMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiGrantMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiReleaseMapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;
import sm.domain.sys.base.openapi.model.form.OpenApiCatalogTestForm;
import sm.system.exception.BizException;
import sm.system.openapi.OpenApiOperationRegistry;
import sm.system.security.authorization.AdministratorOnly;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenApiCatalogTestServiceTests {
    @Mock private OpenApiReleaseMapper releaseMapper;
    @Mock private OpenApiApplicationMapper applicationMapper;
    @Mock private OpenApiGrantMapper grantMapper;
    @Mock private OpenApiOperationRegistry operationRegistry;
    @Mock private OpenApiRuntimeAccessService runtimeAccessService;
    @Mock private JsonMapper jsonMapper;
    @InjectMocks private OpenApiCatalogTestService service;

    @Test
    void serviceRequiresRealAdministratorIdentityAtThePublicBoundary() {
        assertThat(OpenApiCatalogTestService.class).hasAnnotation(AdministratorOnly.class);
    }

    @Test
    void rejectsOperationWithoutExplicitTestHandlerBeforeAnyBusinessExecution() {
        OpenApiReleaseEntity release = new OpenApiReleaseEntity();
        release.setId(1L);
        release.setOperationKey("not.testable");
        release.setStatus("PUBLISHED");
        when(releaseMapper.selectById(1L)).thenReturn(release);
        when(operationRegistry.findByKey("not.testable")).thenReturn(null);

        assertThatThrownBy(() -> service.execute(new OpenApiCatalogTestForm(1L, 2L, "{}")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("未开放管理端业务试调");
        verifyNoInteractions(runtimeAccessService, jsonMapper);
    }
}
