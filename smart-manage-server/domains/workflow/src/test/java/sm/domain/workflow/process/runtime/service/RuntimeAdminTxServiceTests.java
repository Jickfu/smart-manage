package sm.domain.workflow.process.runtime.service;

import org.junit.jupiter.api.Test;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.notification.service.NotificationService;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness;
import sm.domain.workflow.process.runtime.mapper.WorkflowOperationMapper;
import sm.domain.workflow.process.runtime.model.entity.WorkflowOperationEntity;
import sm.domain.workflow.process.runtime.model.form.ScriptRetryForm;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuntimeAdminTxServiceTests {
    @Test
    void failedScriptRetryKeepsAuditWithoutSavingSuccessReceiptOrNotification() {
        var fixture = fixture(false);

        var retry = fixture.service().retryScript(fixture.form(), 10L);

        assertFalse(retry.succeeded());
        verify(fixture.operations()).insert(any(WorkflowOperationEntity.class));
        verify(fixture.commands(), never()).save(any(), any(), any(), any());
        verify(fixture.notifications(), never()).record(any(), any());
    }

    @Test
    void successfulScriptRetrySavesReceiptAndNotification() {
        var fixture = fixture(true);

        var retry = fixture.service().retryScript(fixture.form(), 10L);

        assertTrue(retry.succeeded());
        verify(fixture.operations()).insert(any(WorkflowOperationEntity.class));
        verify(fixture.commands()).save(fixture.form().requestId(), 10L, "digest", retry.run());
        verify(fixture.notifications()).record(fixture.reference(), retry.run());
    }

    private static Fixture fixture(boolean succeeded) {
        WorkflowEngine engine = mock(WorkflowEngine.class);
        InstanceService instances = mock(InstanceService.class);
        WorkflowBusinessRegistry businesses = mock(WorkflowBusinessRegistry.class);
        WorkflowOperationMapper operations = mock(WorkflowOperationMapper.class);
        RuntimeCommandWriter commands = mock(RuntimeCommandWriter.class);
        NotificationService notifications = mock(NotificationService.class);
        WorkflowBusiness business = mock(WorkflowBusiness.class);
        var service = new RuntimeAdminTxService(engine, instances, businesses, operations, commands,
                JsonMapper.builder().build(), notifications);
        var form = new ScriptRetryForm(100L, 200L, "再次执行", UUID.randomUUID());
        var reference = new InstanceService.Reference(100L, "demo", 400L, "BILL-1", 1L, 10L, "{}");
        var before = run(false);
        var after = run(succeeded);
        when(commands.digest(form)).thenReturn("digest");
        when(commands.replay(form.requestId(), 10L, "digest")).thenReturn(null);
        when(instances.reference(100L)).thenReturn(reference);
        when(businesses.require("demo")).thenReturn(business);
        when(engine.inspect(100L)).thenReturn(before);
        when(engine.retryScript(100L, 200L, 10L)).thenReturn(new WorkflowEngine.ScriptRetry(after, succeeded));
        return new Fixture(service, form, reference, operations, commands, notifications);
    }

    private static WorkflowEngine.Run run(boolean active) {
        return new WorkflowEngine.Run(100L, 300L, 10L, WorkflowEngine.State.APPROVING,
                List.of(), List.of(), List.of(), false, active, Map.of());
    }

    private record Fixture(RuntimeAdminTxService service, ScriptRetryForm form, InstanceService.Reference reference,
                           WorkflowOperationMapper operations, RuntimeCommandWriter commands,
                           NotificationService notifications) { }
}
