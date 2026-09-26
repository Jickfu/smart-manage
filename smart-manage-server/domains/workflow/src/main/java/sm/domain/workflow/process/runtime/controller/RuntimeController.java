package sm.domain.workflow.process.runtime.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.runtime.model.form.ApprovalForm;
import sm.domain.workflow.process.runtime.service.RuntimeService;
import sm.system.form.IdForm;
import sm.system.response.Result;

/** 登录后以任务资格或申请人身份授权，菜单权限不能授予审批权。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/runtime")
public class RuntimeController {
    private final RuntimeService service;
    @PostMapping("/approve")
    public Result<WorkflowEngine.Run> approve(@RequestBody @Valid ApprovalForm form) { return Result.success(service.approve(form)); }
    @PostMapping("/withdraw")
    public Result<WorkflowEngine.Run> withdraw(@RequestBody @Valid sm.domain.workflow.process.runtime.model.form.WithdrawForm form) { return Result.success(service.withdraw(form)); }
}
