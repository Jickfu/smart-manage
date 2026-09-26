package sm.domain.workflow.process.task.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.task.model.form.TaskListForm;
import sm.domain.workflow.process.task.model.vo.*;
import sm.system.response.PageData;
import sm.system.security.context.CurrentUserContext;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("workflowTaskService")
@RequiredArgsConstructor
public class TaskService {
    private final WorkflowEngine engine;
    private final InstanceService instances;
    private final CurrentUserContext currentUser;
    private final ObjectMapper json;
    private final sm.domain.sys.base.user.contract.UserReferenceReader users;
    private final sm.domain.workflow.process.task.mapper.TaskCandidateChangeMapper changes;

    public PageData<TaskListVO> listPage(TaskListForm form) {
        var selected = engine.select(currentUser.getUserId(), form.getBox(), Math.multiplyExact(form.getPageNum() - 1, form.getPageSize()), form.getPageSize());
        var records = new ArrayList<TaskListVO>();
        for (Long id : selected.instanceIds()) {
            var reference = instances.reference(id);
            var run = engine.inspect(id);
            records.add(new TaskListVO(id, reference.businessType(), reference.businessId(), reference.number(), run.state(),
                    run.tasks().stream().map(WorkflowEngine.Task::name).collect(Collectors.joining("、"))));
        }
        return PageData.of(selected.total(), form.getPageNum(), form.getPageSize(), records);
    }

    public ApprovalDetailVO detail(Long id) {
        var run = instances.readable(id);
        var reference = instances.reference(id);
        Long actor = currentUser.getUserId();
        Long taskId = run.tasks().stream().filter(task -> task.candidates().contains(actor)).map(WorkflowEngine.Task::id).findFirst().orElse(null);
        boolean canWithdraw = Objects.equals(reference.applicantId(), actor) && run.state() == WorkflowEngine.State.APPROVING && !run.approvalStarted();
        var actorIds = new java.util.HashSet<Long>();
        actorIds.add(reference.applicantId());
        run.tasks().forEach(task -> actorIds.addAll(task.candidates()));
        run.history().forEach(history -> { if (history.actorId() != null) actorIds.add(history.actorId()); });
        var audits = changes.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<sm.domain.workflow.process.task.model.entity.TaskCandidateChangeEntity>()
                .eq(sm.domain.workflow.process.task.model.entity.TaskCandidateChangeEntity::getInstanceId, id)
                .orderByAsc(sm.domain.workflow.process.task.model.entity.TaskCandidateChangeEntity::getCreateTime));
        var history = audits.stream().map(audit -> new CandidateChangeVO(audit.getId(), audit.getTaskId(), audit.getOperatorId(),
                candidateIds(audit.getBeforeCandidates()), candidateIds(audit.getAfterCandidates()), audit.getReason(), audit.getCreateTime())).toList();
        history.forEach(audit -> {
            actorIds.add(audit.operatorId());
            actorIds.addAll(audit.beforeCandidates());
            actorIds.addAll(audit.afterCandidates());
        });
        var names = users.findByIds(actorIds).values().stream().collect(Collectors.toMap(sm.domain.sys.base.user.contract.UserReference::id, sm.domain.sys.base.user.contract.UserReference::name));
        return new ApprovalDetailVO(id, reference.businessType(), reference.businessId(), reference.number(), run, canWithdraw, taskId, names, history);
    }

    public JsonNode chart(Long id) {
        instances.readable(id);
        return json.readTree(engine.chart(id));
    }

    public long pendingCount() { return engine.select(currentUser.getUserId(), WorkflowEngine.Box.PENDING, 0, 1).total(); }

    private java.util.List<Long> candidateIds(String value) {
        return json.readValue(value, json.getTypeFactory().constructCollectionType(java.util.List.class, Long.class));
    }
}
