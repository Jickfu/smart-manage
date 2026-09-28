package sm.domain.workflow.process.task.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.system.security.context.CurrentUserContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskServiceTests {

    @Test
    void participantDetailReturnsOnlyBusinessApprovalHistory() {
        WorkflowEngine engine = mock(WorkflowEngine.class);
        InstanceService instances = mock(InstanceService.class);
        CurrentUserContext currentUser = mock(CurrentUserContext.class);
        UserReferenceReader users = mock(UserReferenceReader.class);
        var approval = history(1L, "APPROVED", WorkflowEngine.HistoryCategory.APPROVAL);
        var management = history(2L, "ADMIN_JUMP", WorkflowEngine.HistoryCategory.MANAGEMENT);
        var script = history(3L, "SCRIPT", WorkflowEngine.HistoryCategory.SCRIPT);
        var cooperation = history(4L, "TRANSFER", WorkflowEngine.HistoryCategory.COOPERATION);
        var run = new WorkflowEngine.Run(10L, 2L, 7L, WorkflowEngine.State.APPROVING,
                List.of(), List.of(approval, management, script, cooperation), List.of(), true, true, Map.of());
        when(instances.readable(10L)).thenReturn(run);
        when(instances.reference(10L)).thenReturn(
                new InstanceService.Reference(10L, "demo.leave", 20L, "L-20", 1L, 7L, "{}"));
        when(currentUser.getUserId()).thenReturn(7L);
        when(users.findByIds(org.mockito.ArgumentMatchers.anyCollection())).thenReturn(Map.of());
        var service = new TaskService(engine, instances, currentUser, JsonMapper.builder().build(), users);

        var detail = service.detail(10L);

        assertEquals(List.of(approval), detail.run().history());
    }

    private static WorkflowEngine.History history(Long id, String action,
                                                   WorkflowEngine.HistoryCategory category) {
        return new WorkflowEngine.History(id, "node", "审批", 99L, action, "内容", Instant.now(), category);
    }
}
