package sm.domain.workflow.process.task.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import sm.domain.workflow.process.task.model.form.TaskListForm;
import sm.domain.workflow.process.task.model.vo.*;
import sm.domain.workflow.process.task.service.TaskService;
import sm.system.form.IdForm;
import sm.system.response.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process")
public class TaskController {
    private final TaskService service;
    @PostMapping("/task/listPage")
    public Result<PageData<TaskListVO>> listPage(@RequestBody @Valid TaskListForm form) { return Result.success(service.listPage(form)); }
    @PostMapping("/instance/detail")
    public Result<ApprovalDetailVO> detail(@RequestBody @Valid IdForm form) { return Result.success(service.detail(form.getId())); }
    @GetMapping("/instance/chart/{id}")
    public Result<JsonNode> chart(@PathVariable Long id) { return Result.success(service.chart(id)); }
    @GetMapping("/task/count")
    public Result<Long> count() { return Result.success(service.pendingCount()); }
}
