package sm.domain.workflow.process.runtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.runtime.model.form.InstanceCommandForm;
import sm.domain.workflow.process.runtime.model.form.InstanceJumpForm;
import sm.domain.workflow.process.runtime.model.form.VariableUpdateForm;
import sm.domain.workflow.process.runtime.model.form.ScriptRetryForm;
import sm.system.aop.log.BizLog;
import sm.system.security.context.CurrentUserContext;

@Service
@RequiredArgsConstructor
public class RuntimeAdminService {
    private final RuntimeAdminTxService transactions;
    private final CurrentUserContext currentUser;

    @BizLog("挂起流程实例")
    public WorkflowEngine.Run suspend(InstanceCommandForm form) { return transactions.command("SUSPEND", form, currentUser.getUserId()); }
    @BizLog("恢复流程实例")
    public WorkflowEngine.Run resume(InstanceCommandForm form) { return transactions.command("RESUME", form, currentUser.getUserId()); }
    @BizLog("终止流程实例")
    public WorkflowEngine.Run terminate(InstanceCommandForm form) { return transactions.command("TERMINATE", form, currentUser.getUserId()); }
    @BizLog("管理员调整流程节点")
    public WorkflowEngine.Run jump(InstanceJumpForm form) { return transactions.jump(form, currentUser.getUserId()); }
    @BizLog(value = "修改流程变量", recordRequest = false)
    public WorkflowEngine.Run variables(VariableUpdateForm form) { return transactions.variables(form, currentUser.getUserId()); }
    @BizLog("重试流程脚本节点")
    public WorkflowEngine.Run retryScript(ScriptRetryForm form) { return transactions.retryScript(form, currentUser.getUserId()); }
}
