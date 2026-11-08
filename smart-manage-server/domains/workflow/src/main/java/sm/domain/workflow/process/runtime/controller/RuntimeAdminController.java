package sm.domain.workflow.process.runtime.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.workflow.process.instance.constant.WorkflowInstancePermission;
import sm.domain.workflow.process.runtime.model.form.InstanceCommandForm;
import sm.domain.workflow.process.runtime.model.form.InstanceJumpForm;
import sm.domain.workflow.process.runtime.model.form.VariableUpdateForm;
import sm.domain.workflow.process.runtime.model.form.ScriptRetryForm;
import sm.domain.workflow.process.runtime.service.RuntimeAdminService;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/instance")
public class RuntimeAdminController {
    private final RuntimeAdminService service;
    @PostMapping("/suspend") @SaCheckPermission(WorkflowInstancePermission.SUSPEND)
    public Result<Void> suspend(@RequestBody @Valid InstanceCommandForm form) {
        service.suspend(form);
        return Result.success();
    }
    @PostMapping("/resume") @SaCheckPermission(WorkflowInstancePermission.RESUME)
    public Result<Void> resume(@RequestBody @Valid InstanceCommandForm form) {
        service.resume(form);
        return Result.success();
    }
    @PostMapping("/terminate") @SaCheckPermission(WorkflowInstancePermission.TERMINATE)
    public Result<Void> terminate(@RequestBody @Valid InstanceCommandForm form) {
        service.terminate(form);
        return Result.success();
    }
    @PostMapping("/jump") @SaCheckPermission(WorkflowInstancePermission.JUMP)
    public Result<Void> jump(@RequestBody @Valid InstanceJumpForm form) {
        service.jump(form);
        return Result.success();
    }
    @PostMapping("/variables") @SaCheckPermission(WorkflowInstancePermission.VARIABLES)
    public Result<Void> variables(@RequestBody @Valid VariableUpdateForm form) {
        service.variables(form);
        return Result.success();
    }
    @PostMapping("/script-retry") @SaCheckPermission(WorkflowInstancePermission.SCRIPT_RETRY)
    public Result<Void> retryScript(@RequestBody @Valid ScriptRetryForm form) {
        service.retryScript(form);
        return Result.success();
    }
}
