package sm.domain.workflow.process.assignment.controller;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.workflow.process.assignment.model.form.*;
import sm.domain.workflow.process.assignment.model.vo.AssignmentOptionVO;
import sm.domain.workflow.process.assignment.service.AssignmentChoiceService;
import sm.domain.workflow.process.definition.constant.WorkflowDefinitionPermission;
import sm.domain.workflow.process.task.constant.TaskPermission;
import sm.system.response.Result;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/assignment")
@SaCheckPermission(value = {WorkflowDefinitionPermission.DESIGN, TaskPermission.MAINTAIN}, mode = SaMode.OR)
public class AssignmentController {
    private final AssignmentChoiceService service;
    @GetMapping("/types")
    public Result<List<String>> types() { return Result.success(service.types()); }
    @PostMapping("/candidates")
    public Result<Map<String, Object>> candidates(@RequestBody @Valid AssignmentQueryForm form) { return Result.success(service.candidates(form)); }
    @PostMapping("/feedback")
    public Result<List<AssignmentOptionVO>> feedback(@RequestBody @Valid AssignmentFeedbackForm form) { return Result.success(service.feedback(form)); }
    @GetMapping("/rules")
    public Result<List<Object>> rules() { return Result.success(List.of()); }
}
