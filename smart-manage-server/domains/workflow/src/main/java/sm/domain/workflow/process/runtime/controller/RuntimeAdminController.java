package sm.domain.workflow.process.runtime.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.workflow.process.engine.WorkflowEngine;
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
    public Result<WorkflowEngine.Run> suspend(@RequestBody @Valid InstanceCommandForm form) { return Result.success(service.suspend(form)); }
    @PostMapping("/resume") @SaCheckPermission(WorkflowInstancePermission.RESUME)
    public Result<WorkflowEngine.Run> resume(@RequestBody @Valid InstanceCommandForm form) { return Result.success(service.resume(form)); }
    @PostMapping("/terminate") @SaCheckPermission(WorkflowInstancePermission.TERMINATE)
    public Result<WorkflowEngine.Run> terminate(@RequestBody @Valid InstanceCommandForm form) { return Result.success(service.terminate(form)); }
    @PostMapping("/jump") @SaCheckPermission(WorkflowInstancePermission.JUMP)
    public Result<WorkflowEngine.Run> jump(@RequestBody @Valid InstanceJumpForm form) { return Result.success(service.jump(form)); }
    @PostMapping("/variables") @SaCheckPermission(WorkflowInstancePermission.VARIABLES)
    public Result<WorkflowEngine.Run> variables(@RequestBody @Valid VariableUpdateForm form) { return Result.success(service.variables(form)); }
    @PostMapping("/script-retry") @SaCheckPermission(WorkflowInstancePermission.SCRIPT_RETRY)
    public Result<WorkflowEngine.Run> retryScript(@RequestBody @Valid ScriptRetryForm form) { return Result.success(service.retryScript(form)); }
}
