package sm.domain.workflow.process.task.controller;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.workflow.process.task.constant.TaskPermission;
import sm.domain.workflow.process.task.model.form.TaskCandidateForm;
import sm.domain.workflow.process.task.service.TaskMaintenanceService;
import sm.system.form.IdForm;
import sm.system.response.Result;
@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/task")
public class TaskMaintenanceController {
    private final TaskMaintenanceService service;
    @PostMapping("/maintenance-target")
    @SaCheckPermission(TaskPermission.MAINTAIN)
    public Result<TaskMaintenanceService.MaintenanceTarget> target(@RequestBody @Valid IdForm form) { return Result.success(service.target(form.getId())); }
    @PostMapping("/candidates")
    @SaCheckPermission(TaskPermission.MAINTAIN)
    public Result<String> replace(@RequestBody @Valid TaskCandidateForm form) { service.replace(form); return Result.success(); }
}
