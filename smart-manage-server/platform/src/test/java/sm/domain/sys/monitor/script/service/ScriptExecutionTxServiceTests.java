package sm.domain.sys.monitor.script.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScriptExecutionTxServiceTests {
    @Test
    void shouldThrowFailureSoSpringRollsBackAtomicExecution() {
        ScriptExecutor executor = mock(ScriptExecutor.class);
        ScriptExecutionConfig config = new ScriptExecutionConfig("ATOMIC", 30, 1000);
        ScriptExecutionOutcome outcome = new ScriptExecutionOutcome("ERROR", "", "failed", 10, false);
        when(executor.execute(config, "throw error")).thenReturn(outcome);

        ScriptExecutionTxService txService = new ScriptExecutionTxService(executor);

        assertThatThrownBy(() -> txService.execute(config, "throw error"))
                .isInstanceOf(ScriptExecutionFailure.class)
                .hasMessage("failed");
    }
}
