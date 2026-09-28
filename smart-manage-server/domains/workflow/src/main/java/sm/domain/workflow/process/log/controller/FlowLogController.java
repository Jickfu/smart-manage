package sm.domain.workflow.process.log.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.workflow.process.log.constant.FlowLogPermission;
import sm.domain.workflow.process.log.model.form.FlowLogListForm;
import sm.domain.workflow.process.log.model.vo.FlowLogRow;
import sm.domain.workflow.process.log.service.FlowLogService;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/flow-log")
public class FlowLogController {
    private final FlowLogService service;

    @PostMapping("/listPage")
    @SaCheckPermission(FlowLogPermission.LIST)
    public Result<PageData<FlowLogRow>> listPage(@RequestBody @Valid FlowLogListForm form) {
        return Result.success(service.listPage(form));
    }
}
