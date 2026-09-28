package sm.domain.workflow.process.instance.service;

import org.junit.jupiter.api.Test;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.mapper.WorkflowInstanceMapper;
import sm.domain.workflow.process.instance.model.entity.WorkflowInstanceEntity;
import sm.system.exception.BizException;
import sm.system.security.context.CurrentUserContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InstanceServiceTests {

    @Test
    void managementHistoryDoesNotGrantBusinessSnapshotAccess() {
        Fixture fixture = fixture(WorkflowEngine.HistoryCategory.MANAGEMENT);

        assertThrows(BizException.class, () -> fixture.service().readable(10L));
    }

    @Test
    void cooperationHistoryStillGrantsParticipantAccess() {
        Fixture fixture = fixture(WorkflowEngine.HistoryCategory.COOPERATION);

        assertSame(fixture.run(), fixture.service().readable(10L));
    }

    private static Fixture fixture(WorkflowEngine.HistoryCategory category) {
        WorkflowInstanceMapper mapper = mock(WorkflowInstanceMapper.class);
        WorkflowEngine engine = mock(WorkflowEngine.class);
        CurrentUserContext currentUser = mock(CurrentUserContext.class);
        WorkflowInstanceEntity entity = new WorkflowInstanceEntity();
        entity.setId(10L);
        entity.setBusinessType("demo.leave");
        entity.setBusinessId(20L);
        entity.setNumber("L-20");
        entity.setOrgId(1L);
        entity.setApplicantId(7L);
        entity.setSnapshot("{}");
        WorkflowEngine.History history = new WorkflowEngine.History(1L, "approve", "审批", 99L,
                category == WorkflowEngine.HistoryCategory.MANAGEMENT ? "TERMINATED" : "TRANSFER",
                "原因", Instant.now(), category);
        WorkflowEngine.Run run = new WorkflowEngine.Run(10L, 2L, 7L, WorkflowEngine.State.TERMINATED,
                List.of(), List.of(history), List.of(), true, false, Map.of());
        when(mapper.selectById(10L)).thenReturn(entity);
        when(engine.inspect(10L)).thenReturn(run);
        when(currentUser.getUserId()).thenReturn(99L);
        return new Fixture(new InstanceService(mapper, engine, currentUser), run);
    }

    private record Fixture(InstanceService service, WorkflowEngine.Run run) { }
}
