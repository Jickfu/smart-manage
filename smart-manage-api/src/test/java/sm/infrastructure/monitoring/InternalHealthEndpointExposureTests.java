package sm.infrastructure.monitoring;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.EndpointExposure;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointWebExtension;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.boot.health.autoconfigure.registry.HealthContributorRegistryAutoConfiguration;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class InternalHealthEndpointExposureTests {
    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EndpointAutoConfiguration.class, HealthContributorRegistryAutoConfiguration.class,
                    HealthEndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class))
            .withPropertyValues("management.endpoints.web.exposure.exclude=*", "spring.jmx.enabled=false")
            .withBean("testHealthIndicator", HealthIndicator.class, () -> () -> Health.up().build());

    @Test
    void healthBeanExistsWithoutWebExposure() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(HealthEndpoint.class);
            assertThat(context.getBean(HealthEndpoint.class).health().getStatus().getCode()).isEqualTo("UP");
            assertThat(context).doesNotHaveBean(HealthEndpointWebExtension.class);
            assertThat(context.getBean(WebEndpointsSupplier.class).getEndpoints()).isEmpty();
        });
    }

    @Test
    void explicitAccessDisableIsNotOverridden() {
        runner.withPropertyValues("management.endpoint.health.access=NONE").run(context ->
                assertThat(context).hasNotFailed().doesNotHaveBean(HealthEndpoint.class));
    }

    @Test
    void contributorDoesNotEnableTransportOrOtherEndpoints() {
        InternalHealthEndpointExposure contributor = new InternalHealthEndpointExposure();
        var message = ConditionMessage.forCondition("internal health");
        assertThat(contributor.getExposureOutcome(EndpointId.of("health"), EnumSet.of(EndpointExposure.WEB), message)).isNull();
        assertThat(contributor.getExposureOutcome(EndpointId.of("health"), EnumSet.of(EndpointExposure.JMX), message)).isNull();
        assertThat(contributor.getExposureOutcome(EndpointId.of("env"), EnumSet.allOf(EndpointExposure.class), message)).isNull();
    }
}
