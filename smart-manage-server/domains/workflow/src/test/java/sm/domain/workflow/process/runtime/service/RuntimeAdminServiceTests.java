package sm.domain.workflow.process.runtime.service;

import org.junit.jupiter.api.Test;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.runtime.model.form.ScriptRetryForm;
import sm.system.exception.BizException;
import sm.system.security.context.CurrentUserContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuntimeAdminServiceTests {
    @Test
    void reportsFailedScriptRetryAfterTransactionalEvidenceIsCommitted() {
        RuntimeAdminTxService transactions = mock(RuntimeAdminTxService.class);
        CurrentUserContext currentUser = mock(CurrentUserContext.class);
        var service = new RuntimeAdminService(transactions, currentUser);
        var form = new ScriptRetryForm(100L, 200L, "再次执行", UUID.randomUUID());
        var run = run(false);
        when(currentUser.getUserId()).thenReturn(10L);
        when(transactions.retryScript(form, 10L)).thenReturn(new WorkflowEngine.ScriptRetry(run, false));

        BizException failure = assertThrows(BizException.class, () -> service.retryScript(form));

        assertTrue(failure.getMessage().contains("脚本执行仍然失败，请检查流程变量或脚本后重试"));
        verify(transactions).retryScript(form, 10L);
    }

    @Test
    void returnsRunAfterSuccessfulScriptRetry() {
        RuntimeAdminTxService transactions = mock(RuntimeAdminTxService.class);
        CurrentUserContext currentUser = mock(CurrentUserContext.class);
        var service = new RuntimeAdminService(transactions, currentUser);
        var form = new ScriptRetryForm(100L, 200L, "再次执行", UUID.randomUUID());
        var run = run(true);
        when(currentUser.getUserId()).thenReturn(10L);
        when(transactions.retryScript(form, 10L)).thenReturn(new WorkflowEngine.ScriptRetry(run, true));

        assertSame(run, service.retryScript(form));
    }

    private static WorkflowEngine.Run run(boolean active) {
        return new WorkflowEngine.Run(100L, 300L, 10L, WorkflowEngine.State.APPROVING,
                List.of(), List.of(), List.of(), false, active, Map.of());
    }
}
