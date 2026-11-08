package sm.domain.workflow.process.runtime.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.workflow.process.runtime.model.form.ApprovalForm;
import sm.domain.workflow.process.runtime.service.RuntimeService;
import sm.system.response.Result;

/** 登录后以任务资格或申请人身份授权，菜单权限不能授予审批权。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/runtime")
public class RuntimeController {
    private final RuntimeService service;
    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody @Valid ApprovalForm form) {
        service.approve(form);
        return Result.success();
    }
    @PostMapping("/withdraw")
    public Result<Void> withdraw(@RequestBody @Valid sm.domain.workflow.process.runtime.model.form.WithdrawForm form) {
        service.withdraw(form);
        return Result.success();
    }
    @PostMapping("/cooperate")
    public Result<Void> cooperate(@RequestBody @Valid sm.domain.workflow.process.runtime.model.form.TaskCooperationForm form) {
        service.cooperate(form);
        return Result.success();
    }
    @PostMapping("/take-back")
    public Result<Void> takeBack(@RequestBody @Valid sm.domain.workflow.process.runtime.model.form.InstanceCommandForm form) {
        service.takeBack(form);
        return Result.success();
    }
}
