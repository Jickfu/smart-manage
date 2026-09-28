package sm.domain.workflow.process.task.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.workflow.process.task.constant.TaskPermission;
import sm.domain.workflow.process.task.model.form.TaskMonitorListForm;
import sm.domain.workflow.process.task.model.vo.TaskMonitorVO;
import sm.domain.workflow.process.task.service.TaskMonitorService;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/task/monitor")
public class TaskMonitorController {
    private final TaskMonitorService service;

    @PostMapping("/listPage")
    @SaCheckPermission(TaskPermission.LIST)
    public Result<PageData<TaskMonitorVO>> listPage(@RequestBody @Valid TaskMonitorListForm form) {
        return Result.success(service.listPage(form));
    }
}
