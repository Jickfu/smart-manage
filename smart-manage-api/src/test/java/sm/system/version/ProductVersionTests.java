package sm.system.version;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.info.BuildProperties;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ProductVersionTests {
    @Test
    void returnsArtifactSnapshotAndDisablesHttpCaching() {
        ProductVersionService service = service("1.2.3");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ProductVersion actual = new ProductVersionController(service).current(response).getData();
        assertEquals(new ProductVersion("1.2.3"), actual);
        assertEquals("no-store", response.getHeader("Cache-Control"));
    }

    @Test
    void unfilteredResourceDoesNotExposePlaceholderAsVersion() {
        assertNull(service("@project.version@").current().version());
    }

    @Test
    void missingMetadataReportsUnavailable() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        assertEquals(new ProductVersion(null),
                new ProductVersionService(factory.getBeanProvider(BuildProperties.class)).current());
    }

    private ProductVersionService service(String version) {
        Properties properties = new Properties();
        properties.setProperty("version", version);
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerSingleton("buildProperties", new BuildProperties(properties));
        return new ProductVersionService(factory.getBeanProvider(BuildProperties.class));
    }
}
