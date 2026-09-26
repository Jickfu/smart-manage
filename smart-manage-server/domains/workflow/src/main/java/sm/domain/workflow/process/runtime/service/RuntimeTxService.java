package sm.domain.workflow.process.runtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness;
import sm.domain.workflow.process.runtime.model.form.ApprovalForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class RuntimeTxService {
    private final InstanceService instances;
    private final WorkflowEngine engine;
    private final WorkflowBusinessRegistry businesses;
    private final UserReferenceReader users;
    private final sm.domain.workflow.process.notification.service.NotificationService notifications;
    private final RuntimeCommandWriter commands;

    WorkflowEngine.Run approve(ApprovalForm form, Long actorId) {
        users.requireEnabled(actorId);
        String digest = commands.digest(form.action(), form.instanceId(), form.taskId(), form.opinion());
        var receipt = commands.replay(form.requestId(), actorId, digest);
        if (receipt != null) return receipt;
        var reference = instances.reference(form.instanceId());
        var business = businesses.require(reference.businessType());
        business.lock(reference.businessId(), reference.id());
        if ("REJECT".equals(form.action()) && (form.opinion() == null || form.opinion().isBlank())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "拒绝时请填写审批意见");
        }
        var result = "APPROVE".equals(form.action())
                ? engine.approve(reference.id(), form.taskId(), actorId, form.opinion())
                : engine.reject(reference.id(), form.taskId(), actorId, form.opinion());
        complete(reference, business, result);
        commands.save(form.requestId(), actorId, digest, result);
        return result;
    }

    WorkflowEngine.Run withdraw(sm.domain.workflow.process.runtime.model.form.WithdrawForm form, Long actorId) {
        users.requireEnabled(actorId);
        Long id = form.id();
        String digest = commands.digest("WITHDRAW", id, null, null);
        var receipt = commands.replay(form.requestId(), actorId, digest);
        if (receipt != null) return receipt;
        var reference = instances.reference(id);
        var business = businesses.require(reference.businessType());
        business.lock(reference.businessId(), reference.id());
        var result = engine.withdraw(id, actorId);
        complete(reference, business, result);
        commands.save(form.requestId(), actorId, digest, result);
        return result;
    }

    private void complete(InstanceService.Reference reference, WorkflowBusiness business, WorkflowEngine.Run run) {
        notifications.record(reference, run);
        if (run.state() != WorkflowEngine.State.APPROVING) {
            business.completed(reference.businessId(), reference.id(), WorkflowBusiness.Outcome.valueOf(run.state().name()));
        }
    }
}
