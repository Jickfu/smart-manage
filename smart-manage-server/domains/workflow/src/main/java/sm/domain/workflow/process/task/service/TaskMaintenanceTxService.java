package sm.domain.workflow.process.task.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.runtime.service.WorkflowBusinessRegistry;
import sm.domain.workflow.process.task.mapper.TaskCandidateChangeMapper;
import sm.domain.workflow.process.task.model.entity.TaskCandidateChangeEntity;
import sm.domain.workflow.process.task.model.form.TaskCandidateForm;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class TaskMaintenanceTxService {
    private final TaskCandidateChangeMapper mapper;
    private final WorkflowEngine engine;
    private final InstanceService instances;
    private final WorkflowBusinessRegistry businesses;
    private final UserReferenceReader users;
    private final ObjectMapper json;
    private final sm.domain.workflow.process.notification.service.NotificationService notifications;
    void replace(TaskCandidateForm form, Long operatorId) {
        var reference = instances.reference(form.instanceId());
        businesses.require(reference.businessType()).lock(reference.businessId(), reference.id());
        var candidates = form.candidateIds().stream().distinct().toList();
        users.requireEnabledByIds(candidates);
        var change = engine.replaceCandidates(form.instanceId(), form.taskId(), operatorId, candidates);
        var audit = new TaskCandidateChangeEntity();
        audit.setInstanceId(form.instanceId());
        audit.setTaskId(form.taskId());
        audit.setOperatorId(operatorId);
        audit.setBeforeCandidates(json.writeValueAsString(change.before().candidates()));
        audit.setAfterCandidates(json.writeValueAsString(change.after().candidates()));
        audit.setReason(form.reason().trim());
        mapper.insert(audit);
        notifications.candidatesChanged(reference, change.after(), audit.getId());
    }
}
