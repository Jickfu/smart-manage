package sm.domain.sys.monitor.thread.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.system.security.authorization.AdministratorOnly;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.common.service.MonitorRoutingGateway;
import sm.domain.sys.monitor.thread.model.form.ThreadCollectForm;
import sm.domain.sys.monitor.thread.model.vo.ThreadDiagnosticVO;
import sm.system.aop.log.BizLog;

/** 多实例线程诊断的唯一公开业务入口。 */
@Service
@AdministratorOnly
@RequiredArgsConstructor
public class ThreadDiagnosticService {
    private final MonitorInstanceRegistry instanceRegistry;
    private final MonitorRoutingGateway routingGateway;
    private final ThreadDiagnosticAccessor accessor;

    public ThreadDiagnosticVO list(String instanceId) {
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(instanceId);
        return instanceRegistry.isCurrent(instance.getInstanceId())
                ? accessor.list()
                : routingGateway.get(instance, "/sys/monitor/internal/thread/list", ThreadDiagnosticVO.class);
    }

    public ThreadDiagnosticVO detail(String instanceId, long threadId, int maxDepth) {
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(instanceId);
        return instanceRegistry.isCurrent(instance.getInstanceId())
                ? accessor.detail(threadId, maxDepth)
                : routingGateway.get(instance,
                "/sys/monitor/internal/thread/" + threadId + "?maxDepth=" + maxDepth,
                ThreadDiagnosticVO.class);
    }

    @BizLog(value = "采集热点线程", recordRequest = false, recordResponse = false)
    public ThreadDiagnosticVO hot(ThreadCollectForm form) {
        int sampleMillis = form.getSampleMillis() == null ? 1000 : form.getSampleMillis();
        int limit = form.getLimit() == null ? 10 : form.getLimit();
        int maxDepth = form.getMaxDepth() == null ? 64 : form.getMaxDepth();
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(form.getInstanceId());
        return instanceRegistry.isCurrent(instance.getInstanceId())
                ? accessor.hot(sampleMillis, limit, maxDepth)
                : routingGateway.post(instance, "/sys/monitor/internal/thread/hot", form, ThreadDiagnosticVO.class);
    }

    @BizLog(value = "采集全量线程快照", recordRequest = false, recordResponse = false)
    public ThreadDiagnosticVO dump(ThreadCollectForm form) {
        int maxDepth = form.getMaxDepth() == null ? 64 : form.getMaxDepth();
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(form.getInstanceId());
        return instanceRegistry.isCurrent(instance.getInstanceId())
                ? accessor.dump(maxDepth)
                : routingGateway.post(instance, "/sys/monitor/internal/thread/dump", form, ThreadDiagnosticVO.class);
    }

    @BizLog(value = "检测线程死锁", recordRequest = false, recordResponse = false)
    public ThreadDiagnosticVO deadlocks(ThreadCollectForm form) {
        int maxDepth = form.getMaxDepth() == null ? 128 : form.getMaxDepth();
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(form.getInstanceId());
        return instanceRegistry.isCurrent(instance.getInstanceId())
                ? accessor.deadlocks(maxDepth)
                : routingGateway.post(instance, "/sys/monitor/internal/thread/deadlocks", form,
                ThreadDiagnosticVO.class);
    }

    public ThreadDiagnosticVO localList() {
        return accessor.list();
    }

    public ThreadDiagnosticVO localDetail(long threadId, int maxDepth) {
        return accessor.detail(threadId, maxDepth);
    }

    public ThreadDiagnosticVO localHot(ThreadCollectForm form) {
        return accessor.hot(form.getSampleMillis() == null ? 1000 : form.getSampleMillis(),
                form.getLimit() == null ? 10 : form.getLimit(),
                form.getMaxDepth() == null ? 64 : form.getMaxDepth());
    }

    public ThreadDiagnosticVO localDump(ThreadCollectForm form) {
        return accessor.dump(form.getMaxDepth() == null ? 64 : form.getMaxDepth());
    }

    public ThreadDiagnosticVO localDeadlocks(ThreadCollectForm form) {
        return accessor.deadlocks(form.getMaxDepth() == null ? 128 : form.getMaxDepth());
    }
}
