package sm.domain.workflow.process.instance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.contract.WorkflowHistoryReader;
import sm.domain.workflow.process.instance.mapper.WorkflowInstanceMapper;
import sm.domain.workflow.process.instance.model.entity.WorkflowInstanceEntity;
import sm.domain.workflow.process.runtime.contract.WorkflowSubmission;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;
import java.util.Objects;
import java.util.UUID;

@Service("workflowInstanceService")
@RequiredArgsConstructor
public class InstanceService implements WorkflowHistoryReader {
    private final WorkflowInstanceMapper mapper;
    private final WorkflowEngine engine;
    private final CurrentUserContext currentUser;

    public Reference reference(Long id) {
        var instance = mapper.selectById(id);
        if (instance == null) throw new BizException(ResultEnum.NOT_FOUND, "审批轮次不存在");
        return new Reference(id, instance.getBusinessType(), instance.getBusinessId(), instance.getNumber(),
                instance.getOrgId(), instance.getApplicantId(), instance.getSnapshot());
    }

    public void register(Long id, WorkflowSubmission.Command command) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("实例关联必须参加业务事务");
        var instance = new WorkflowInstanceEntity();
        instance.setId(id);
        instance.setBusinessType(command.businessType());
        instance.setBusinessId(command.businessId());
        instance.setNumber(command.number());
        instance.setOrgId(command.orgId());
        instance.setApplicantId(command.applicantId());
        instance.setRequestId(command.requestId());
        instance.setRequestDigest(command.requestDigest());
        instance.setSnapshot(command.snapshot());
        mapper.insert(instance);
    }

    public Long findSubmission(UUID requestId, String requestDigest, String businessType, Long businessId, Long applicantId) {
        var instance = mapper.selectOne(new LambdaQueryWrapper<WorkflowInstanceEntity>().eq(WorkflowInstanceEntity::getRequestId, requestId));
        if (instance == null) return null;
        if (!Objects.equals(instance.getBusinessType(), businessType) || !Objects.equals(instance.getBusinessId(), businessId)
                || !Objects.equals(instance.getApplicantId(), applicantId) || !Objects.equals(instance.getRequestDigest(), requestDigest)) throw new BizException(ResultEnum.PARAM_ERROR, "提交请求标识已被其他单据或内容使用");
        return instance.getId();
    }

    public WorkflowSubmission.Receipt submissionReceipt(UUID requestId, String requestDigest) {
        var instance = mapper.selectOne(new LambdaQueryWrapper<WorkflowInstanceEntity>().eq(WorkflowInstanceEntity::getRequestId, requestId));
        if (instance == null) return null;
        if (!Objects.equals(instance.getApplicantId(), currentUser.getUserId())) throw new BizException(ResultEnum.PERMISSION_ERROR, "提交请求不属于当前用户");
        if (!Objects.equals(instance.getRequestDigest(), requestDigest)) throw new BizException(ResultEnum.PARAM_ERROR, "同一提交请求不能更改内容，请核实原提交结果");
        return new WorkflowSubmission.Receipt(instance.getBusinessType(), instance.getBusinessId(), instance.getId());
    }

    public WorkflowEngine.Run readable(Long id) {
        var reference = reference(id);
        var run = engine.inspect(id);
        Long actor = currentUser.getUserId();
        boolean allowed = participant(reference, run, actor);
        if (!allowed) throw new BizException(ResultEnum.PERMISSION_ERROR, "当前用户不能查看此审批轮次");
        return run;
    }

    private boolean participant(Reference reference, WorkflowEngine.Run run, Long actor) {
        return Objects.equals(reference.applicantId(), actor)
                || run.tasks().stream().anyMatch(task -> task.candidates().contains(actor))
                || run.history().stream().anyMatch(history -> Objects.equals(history.actorId(), actor));
    }

    @Override
    public boolean canRead(String businessType, Long businessId, Long instanceId) {
        var reference = reference(instanceId);
        return Objects.equals(reference.businessType(), businessType) && Objects.equals(reference.businessId(), businessId)
                && participant(reference, engine.inspect(instanceId), currentUser.getUserId());
    }

    @Override
    public String requireSnapshot(String businessType, Long businessId, Long instanceId) {
        readable(instanceId);
        var reference = reference(instanceId);
        if (!Objects.equals(reference.businessType(), businessType) || !Objects.equals(reference.businessId(), businessId)) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "审批轮次与单据不匹配");
        }
        return reference.snapshot();
    }

    public record Reference(Long id, String businessType, Long businessId, String number, Long orgId,
                            Long applicantId, String snapshot) { }
}
