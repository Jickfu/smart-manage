package sm.domain.sys.monitor.slowsql.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.common.service.MonitorRoutingGateway;
import sm.domain.sys.monitor.slowsql.model.form.SlowSqlCommandForm;
import sm.domain.sys.monitor.slowsql.model.form.SlowSqlTargetForm;
import sm.domain.sys.monitor.slowsql.model.vo.SlowSqlSnapshotVO;
import sm.system.aop.log.BizLog;

/** 多实例 Druid 慢 SQL 统计的唯一公开业务入口。 */
@Service
@RequiredArgsConstructor
public class SlowSqlService {
    private final CurrentUserContext currentUserContext;
    private final MonitorInstanceRegistry instanceRegistry;
    private final MonitorRoutingGateway routingGateway;
    private final SlowSqlStatAccessor accessor;

    @Value("${smart-manage.instance-id}")
    private String currentInstanceId;

    public SlowSqlSnapshotVO snapshot(String instanceId) {
        currentUserContext.checkAdministrator();
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(instanceId);
        return instanceRegistry.isCurrent(instance.getInstanceId())
                ? accessor.snapshot(currentInstanceId)
                : routingGateway.get(instance, "/sys/monitor/internal/slow-sql/snapshot", SlowSqlSnapshotVO.class);
    }

    @BizLog(value = "调整慢SQL监控阈值", recordResponse = false)
    public SlowSqlSnapshotVO updateThreshold(SlowSqlCommandForm form) {
        currentUserContext.checkAdministrator();
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(form.getInstanceId());
        return instanceRegistry.isCurrent(instance.getInstanceId())
                ? accessor.updateThreshold(currentInstanceId, form.getThresholdMs())
                : routingGateway.post(instance, "/sys/monitor/internal/slow-sql/threshold", form,
                SlowSqlSnapshotVO.class);
    }

    @BizLog(value = "清空慢SQL内存统计", recordRequest = false, recordResponse = false)
    public SlowSqlSnapshotVO clear(SlowSqlTargetForm form) {
        currentUserContext.checkAdministrator();
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(form.getInstanceId());
        return instanceRegistry.isCurrent(instance.getInstanceId())
                ? accessor.clear(currentInstanceId)
                : routingGateway.post(instance, "/sys/monitor/internal/slow-sql/clear", form,
                SlowSqlSnapshotVO.class);
    }

    public SlowSqlSnapshotVO localSnapshot() {
        currentUserContext.checkAdministrator();
        return accessor.snapshot(currentInstanceId);
    }

    public SlowSqlSnapshotVO localUpdateThreshold(SlowSqlCommandForm form) {
        currentUserContext.checkAdministrator();
        return accessor.updateThreshold(currentInstanceId, form.getThresholdMs());
    }

    public SlowSqlSnapshotVO localClear() {
        currentUserContext.checkAdministrator();
        return accessor.clear(currentInstanceId);
    }
}
