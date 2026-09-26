package sm.domain.workflow.process.runtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.domain.workflow.process.definition.service.DefinitionService;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.instance.service.InstanceService;
import sm.domain.workflow.process.runtime.contract.WorkflowSubmission;
import sm.domain.workflow.process.runtime.model.form.ApprovalForm;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;
import java.util.LinkedHashMap;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RuntimeService implements WorkflowSubmission {
    private final DefinitionService definitions;
    private final InstanceService instances;
    private final WorkflowEngine engine;
    private final RuntimeTxService transactions;
    private final CurrentUserContext currentUser;
    private final UserReferenceReader users;
    private final sm.domain.workflow.process.notification.service.NotificationService notifications;

    @Override
    public Receipt findSubmission(java.util.UUID requestId, String requestDigest) { return instances.submissionReceipt(requestId, requestDigest); }

    @Override
    public Long submit(Command command) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("业务提交必须先开启事务并锁定单据");
        if (!Objects.equals(command.applicantId(), currentUser.getUserId())) throw new BizException(ResultEnum.PERMISSION_ERROR, "只能发起本人的审批");
        users.requireEnabled(command.applicantId());
        if (command.requestId() == null || command.requestDigest() == null || !command.requestDigest().matches("[0-9a-f]{64}") || command.orgId() == null || command.businessId() == null
                || command.snapshot() == null || command.snapshot().length() > 1_000_000) throw new BizException(ResultEnum.PARAM_ERROR, "提交资料无效");
        Long previous = instances.findSubmission(command.requestId(), command.requestDigest(), command.businessType(), command.businessId(), command.applicantId());
        if (previous != null) return previous;
        String flowCode = definitions.requireFlowForSubmission(command.businessType());
        var variables = new LinkedHashMap<>(command.variables());
        // 人员规则上下文只由服务端业务单据提供，不接受前端流程变量。
        variables.put("orgId", command.orgId());
        variables.put("applicantId", command.applicantId());
        var run = engine.start(flowCode, command.businessType() + "/" + command.businessId(), command.applicantId(), variables);
        if (run.state() != WorkflowEngine.State.APPROVING) throw new BizException(ResultEnum.PARAM_ERROR, "流程没有生成有效人工审批任务");
        instances.register(run.id(), command);
        notifications.record(instances.reference(run.id()), run);
        return run.id();
    }

    @BizLog(value = "处理审批任务", recordRequest = false)
    public WorkflowEngine.Run approve(ApprovalForm form) { return transactions.approve(form, currentUser.getUserId()); }
    @BizLog("撤回审批流程")
    public WorkflowEngine.Run withdraw(sm.domain.workflow.process.runtime.model.form.WithdrawForm form) { return transactions.withdraw(form, currentUser.getUserId()); }
}
