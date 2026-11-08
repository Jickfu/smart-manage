package sm.domain.workflow.process.runtime.controller;

import org.junit.jupiter.api.Test;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.runtime.model.form.ApprovalForm;
import sm.domain.workflow.process.runtime.model.form.InstanceCommandForm;
import sm.domain.workflow.process.runtime.service.RuntimeAdminService;
import sm.domain.workflow.process.runtime.service.RuntimeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuntimeCommandResponseTests {
    @Test
    void approvalAndManagementResponsesOmitStoredRuntimeVariables() {
        var storedRun = new WorkflowEngine.Run(100L, 200L, 10L, WorkflowEngine.State.APPROVING,
                List.of(), List.of(), List.of(), true, true, Map.of("apiToken", "fixture-only"));
        var approval = new ApprovalForm(100L, 300L, "APPROVE", null, UUID.randomUUID());
        var suspend = new InstanceCommandForm(100L, "隔离测试", UUID.randomUUID());
        var runtime = mock(RuntimeService.class);
        var administration = mock(RuntimeAdminService.class);
        when(runtime.approve(approval)).thenReturn(storedRun);
        when(administration.suspend(suspend)).thenReturn(storedRun);

        assertNull(new RuntimeController(runtime).approve(approval).getData());
        assertNull(new RuntimeAdminController(administration).suspend(suspend).getData());
    }
}
