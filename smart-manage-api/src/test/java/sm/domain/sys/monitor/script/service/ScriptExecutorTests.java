package sm.domain.sys.monitor.script.service;

import jakarta.validation.Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import sm.domain.test.DemoService;
import sm.domain.test.ValidationDemoService;
import tools.jackson.databind.json.JsonMapper;
import sm.system.concurrent.DistributedMutex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScriptExecutorTests {
    private ScriptExecutor executor;

    @AfterEach
    void shutdownExecutor() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Test
    void shouldInvokePublicDomainServiceWithStructuredArgument() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("demoService")).thenReturn(new DemoService());
        JsonMapper jsonMapper = JsonMapper.builder().build();
        executor = executor(applicationContext, jsonMapper);

        ScriptExecutionOutcome outcome = executor.execute(new ScriptExecutionConfig("ATOMIC", 5, 10000),
                "const service = app.getService('demoService');\n"
                        + "return service.echo({ name: 'Smart Manage' });");

        assertThat(outcome.status()).as(outcome.errorMessage()).isEqualTo("SUCCESS");
        assertThat(outcome.output()).contains("Smart Manage");
    }

    @Test
    void shouldRejectNotNullViolationBeforeInvokingService() {
        assertInvalidArgument("{ name: 'valid', quantity: 1, nested: { code: 'N' } }", "ID不能为空");
    }

    @Test
    void shouldRejectMethodParameterViolationBeforeInvokingService() {
        assertInvalidArgument("null", "表单不能为空");
    }

    @Test
    void shouldRejectNotBlankViolationBeforeInvokingService() {
        assertInvalidArgument("{ id: 1, name: ' ', quantity: 1, nested: { code: 'N' } }", "名称不能为空");
    }

    @Test
    void shouldRejectLengthViolationBeforeInvokingService() {
        assertInvalidArgument("{ id: 1, name: '123456789', quantity: 1, nested: { code: 'N' } }",
                "名称不能超过8个字符");
    }

    @Test
    void shouldRejectNumericRangeViolationBeforeInvokingService() {
        assertInvalidArgument("{ id: 1, name: 'valid', quantity: 11, nested: { code: 'N' } }",
                "数量不能大于10");
    }

    @Test
    void shouldRejectNestedViolationBeforeInvokingService() {
        assertInvalidArgument("{ id: 1, name: 'valid', quantity: 1, nested: { code: ' ' } }",
                "嵌套编码不能为空");
    }

    @Test
    void shouldRejectNonServiceBean() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("unsafeBean")).thenReturn(new UnsafeBean());
        JsonMapper jsonMapper = JsonMapper.builder().build();
        executor = executor(applicationContext, jsonMapper);

        ScriptExecutionOutcome outcome = executor.execute(new ScriptExecutionConfig("ATOMIC", 5, 10000),
                "return app.getService('unsafeBean');");

        assertThat(outcome.status()).isEqualTo("ERROR");
        assertThat(outcome.errorMessage()).contains("不允许访问该 Bean");
    }

    @Test
    void shouldCancelInfiniteLoopAtDeadline() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        JsonMapper jsonMapper = JsonMapper.builder().build();
        executor = executor(applicationContext, jsonMapper);

        ScriptExecutionOutcome outcome = executor.execute(new ScriptExecutionConfig("ATOMIC", 1, 10000),
                "while (true) {};");

        assertThat(outcome.status()).isEqualTo("TIMEOUT");
        assertThat(outcome.executeDuration()).isGreaterThanOrEqualTo(900);
    }

    @Test
    void shouldTruncateExcessiveOutput() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        JsonMapper jsonMapper = JsonMapper.builder().build();
        executor = executor(applicationContext, jsonMapper);

        ScriptExecutionOutcome outcome = executor.execute(new ScriptExecutionConfig("ATOMIC", 5, 1000),
                "console.log('x'.repeat(5000));");

        assertThat(outcome.status()).isEqualTo("SUCCESS");
        assertThat(outcome.truncated()).isTrue();
        assertThat(outcome.output()).hasSizeLessThanOrEqualTo(1000);
    }

    @Test
    void timeoutDuringHighFrequencyOutputDoesNotRaceWithContextClose() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        JsonMapper jsonMapper = JsonMapper.builder().build();
        executor = executor(applicationContext, jsonMapper);

        ScriptExecutionOutcome outcome = executor.execute(new ScriptExecutionConfig("ATOMIC", 1, 4096),
                "while (true) { console.log('0123456789'.repeat(100)); }");

        assertThat(outcome.status()).isEqualTo("TIMEOUT");
        assertThat(outcome.output()).hasSizeLessThanOrEqualTo(4096);
    }

    public static class UnsafeBean {
    }

    private void assertInvalidArgument(String argument, String expectedMessage) {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        ValidationDemoService demoService = new ValidationDemoService();
        when(applicationContext.getBean("validationDemoService")).thenReturn(demoService);
        JsonMapper jsonMapper = JsonMapper.builder().build();
        executor = executor(applicationContext, jsonMapper);

        ScriptExecutionOutcome outcome = executor.execute(new ScriptExecutionConfig("ATOMIC", 5, 10000),
                "const service = app.getService('validationDemoService'); return service.validate(" + argument + ");");

        assertThat(outcome.status()).isEqualTo("ERROR");
        assertThat(outcome.errorMessage()).contains(expectedMessage);
        assertThat(demoService.invocationCount()).isZero();
    }

    private ScriptExecutor executor(ApplicationContext applicationContext, JsonMapper jsonMapper) {
        ScriptServiceCatalog catalog = new ScriptServiceCatalog(applicationContext, jsonMapper);
        DistributedMutex mutex = (namespace, key) -> () -> { };
        return new ScriptExecutor(new ScriptServiceGateway(catalog, jsonMapper,
                Validation.buildDefaultValidatorFactory().getValidator()), jsonMapper, mutex);
    }
}
