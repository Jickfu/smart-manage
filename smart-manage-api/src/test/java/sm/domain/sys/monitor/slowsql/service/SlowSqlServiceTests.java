package sm.domain.sys.monitor.slowsql.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.common.service.MonitorRoutingGateway;
import sm.domain.sys.monitor.slowsql.model.form.SlowSqlCommandForm;
import sm.domain.sys.monitor.slowsql.model.form.SlowSqlTargetForm;
import sm.domain.sys.monitor.slowsql.model.vo.SlowSqlSnapshotVO;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SlowSqlServiceTests {
    private CurrentUserContext currentUserContext;
    private MonitorInstanceRegistry instanceRegistry;
    private SlowSqlStatAccessor accessor;
    private SlowSqlService service;

    @BeforeEach
    void setUp() {
        currentUserContext = mock(CurrentUserContext.class);
        instanceRegistry = mock(MonitorInstanceRegistry.class);
        accessor = mock(SlowSqlStatAccessor.class);
        service = new SlowSqlService(currentUserContext, instanceRegistry,
                mock(MonitorRoutingGateway.class), accessor);
        ReflectionTestUtils.setField(service, "currentInstanceId", "instance1");
        MonitorInstanceRegistry.RegisteredInstance instance = new MonitorInstanceRegistry.RegisteredInstance();
        instance.setInstanceId("instance1");
        when(instanceRegistry.require("instance1")).thenReturn(instance);
        when(instanceRegistry.isCurrent("instance1")).thenReturn(true);
    }

    @Test
    void updateThresholdChecksAdministratorBeforeChangingLocalStatFilter() {
        SlowSqlCommandForm form = new SlowSqlCommandForm();
        form.setInstanceId("instance1");
        form.setThresholdMs(1500L);
        SlowSqlSnapshotVO expected = new SlowSqlSnapshotVO();
        when(accessor.updateThreshold("instance1", 1500L)).thenReturn(expected);

        SlowSqlSnapshotVO result = service.updateThreshold(form);

        assertSame(expected, result);
        verify(currentUserContext).checkAdministrator();
        verify(accessor).updateThreshold("instance1", 1500L);
    }

    @Test
    void clearChecksAdministratorBeforeClearingLocalStatistics() {
        SlowSqlTargetForm form = new SlowSqlTargetForm();
        form.setInstanceId("instance1");
        SlowSqlSnapshotVO expected = new SlowSqlSnapshotVO();
        when(accessor.clear("instance1")).thenReturn(expected);

        SlowSqlSnapshotVO result = service.clear(form);

        assertSame(expected, result);
        verify(currentUserContext).checkAdministrator();
        verify(accessor).clear("instance1");
    }
}
