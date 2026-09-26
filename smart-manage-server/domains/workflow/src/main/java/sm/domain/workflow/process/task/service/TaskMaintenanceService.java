package sm.domain.workflow.process.task.service;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.task.constant.TaskPermission;
import sm.domain.workflow.process.task.model.form.TaskCandidateForm;
import sm.system.aop.log.BizLog;
import sm.system.security.context.CurrentUserContext;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskMaintenanceService {
    private final TaskMaintenanceTxService transactions;
    private final InstanceService instances;
    private final WorkflowEngine engine;
    private final CurrentUserContext currentUser;
    /** 维护入口只读取路由所需的单号和活动任务，不返回业务快照、意见和附件。 */
    public MaintenanceTarget target(Long id) {
        StpUtil.checkPermission(TaskPermission.MAINTAIN);
        var reference = instances.reference(id);
        return new MaintenanceTarget(id, reference.number(), engine.inspect(id).tasks());
    }
    @BizLog(value = "维护任务候选人", recordRequest = false)
    public void replace(TaskCandidateForm form) {
        StpUtil.checkPermission(TaskPermission.MAINTAIN);
        transactions.replace(form, currentUser.getUserId());
    }
    public record MaintenanceTarget(Long id, String number, List<WorkflowEngine.Task> tasks) { }
}
