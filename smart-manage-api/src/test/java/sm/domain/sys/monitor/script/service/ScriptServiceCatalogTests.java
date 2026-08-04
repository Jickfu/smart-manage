package sm.domain.sys.monitor.script.service;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import sm.domain.test.DemoService;
import sm.domain.sys.monitor.script.model.vo.ScriptApiServiceVO;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScriptServiceCatalogTests {
    @Test
    void metadataMustUseTheSameAllowedServiceRuleAsExecution() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(org.springframework.stereotype.Service.class))
                .thenReturn(Map.of("demoService", new DemoService(), "unsafeBean", new Object()));
        ScriptServiceCatalog catalog = new ScriptServiceCatalog(applicationContext, JsonMapper.builder().build());

        List<ScriptApiServiceVO> services = catalog.metadata();

        assertThat(services).singleElement().satisfies(service -> {
            assertThat(service.getBeanName()).isEqualTo("demoService");
            assertThat(service.getMethods()).singleElement().satisfies(method -> {
                assertThat(method.getName()).isEqualTo("echo");
                assertThat(method.getParameters()).singleElement().satisfies(parameter -> {
                    assertThat(parameter.getName()).isEqualTo("form");
                    assertThat(parameter.getFields()).extracting("name").containsExactly("name");
                });
                assertThat(method.getExample()).contains("app.getService('demoService')", "service.echo");
            });
        });
    }
}
