package sm.domain.workflow.process.instance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.user.contract.UserReference;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.mapper.WorkflowInstanceMapper;
import sm.domain.workflow.process.instance.model.entity.WorkflowInstanceEntity;
import sm.domain.workflow.process.instance.model.form.InstanceListForm;
import sm.domain.workflow.process.instance.model.vo.InstanceAdminDetailVO;
import sm.domain.workflow.process.instance.model.vo.InstanceListVO;
import sm.domain.workflow.process.runtime.service.WorkflowOperationQueryService;
import sm.domain.workflow.process.task.service.TaskCandidateHistoryService;
import sm.system.response.PageData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

/** 管理员监控读取模型。它只组合领域投影，不向外暴露 Warm-Flow 实体。 */
@Service
@RequiredArgsConstructor
public class InstanceManagementService {
    private final WorkflowInstanceMapper instances;
    private final InstanceService instanceService;
    private final WorkflowEngine engine;
    private final TaskCandidateHistoryService candidateChanges;
    private final WorkflowOperationQueryService operations;
    private final UserReferenceReader users;
    private final WorkflowVariableMasker variableMasker;

    public PageData<InstanceListVO> listPage(InstanceListForm form) {
        var query = new LambdaQueryWrapper<WorkflowInstanceEntity>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            query.and(filter -> filter.like(WorkflowInstanceEntity::getNumber, keyword)
                    .or().like(WorkflowInstanceEntity::getBusinessType, keyword));
        }
        var page = instances.selectPage(Page.of(form.getPageNum(), form.getPageSize()), query.orderByDesc(WorkflowInstanceEntity::getId));
        var applicantNames = users.findByIds(page.getRecords().stream().map(WorkflowInstanceEntity::getApplicantId).collect(Collectors.toSet()));
        var records = new ArrayList<InstanceListVO>();
        for (var instance : page.getRecords()) {
            var run = engine.inspect(instance.getId());
            records.add(new InstanceListVO(instance.getId(), instance.getBusinessType(), instance.getBusinessId(),
                    instance.getNumber(), instance.getOrgId(), instance.getApplicantId(),
                    applicantNames.containsKey(instance.getApplicantId()) ? applicantNames.get(instance.getApplicantId()).name() : null,
                    run.state(), run.active(),
                    run.tasks().stream().map(WorkflowEngine.Task::name).collect(Collectors.joining("、")),
                    instance.getCreateTime()));
        }
        return PageData.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    public InstanceAdminDetailVO detail(Long id) {
        var reference = instanceService.reference(id);
        var entity = instances.selectById(id);
        var engineRun = engine.inspect(id);
        // 管理权限用于监控和处置流程，不等同于读取流程变量中可能携带的凭据。
        var run = new WorkflowEngine.Run(engineRun.id(), engineRun.definitionId(), engineRun.applicantId(),
                engineRun.state(), engineRun.tasks(), engineRun.history(), engineRun.nodeTargets(),
                engineRun.approvalStarted(), engineRun.active(), variableMasker.mask(engineRun.variables()));
        var changeViews = candidateChanges.listByInstance(id);
        var operationViews = operations.listByInstance(id);
        var actorIds = new HashSet<Long>();
        actorIds.add(reference.applicantId());
        run.tasks().forEach(task -> actorIds.addAll(task.candidates()));
        run.history().forEach(history -> { if (history.actorId() != null) actorIds.add(history.actorId()); });
        changeViews.forEach(change -> {
            actorIds.add(change.operatorId());
            actorIds.addAll(change.beforeCandidates());
            actorIds.addAll(change.afterCandidates());
        });
        operationViews.forEach(operation -> actorIds.add(operation.operatorId()));
        var names = users.findByIds(actorIds).values().stream()
                .collect(Collectors.toMap(UserReference::id, UserReference::name));
        return new InstanceAdminDetailVO(id, reference.businessType(), reference.businessId(), reference.number(),
                entity.getOrgId(), reference.applicantId(), run, names, changeViews, operationViews);
    }
}
