package sm.domain.workflow.process.instance.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.workflow.process.instance.constant.WorkflowInstancePermission;
import sm.domain.workflow.process.instance.model.form.InstanceListForm;
import sm.domain.workflow.process.instance.model.vo.InstanceAdminDetailVO;
import sm.domain.workflow.process.instance.model.vo.InstanceListVO;
import sm.domain.workflow.process.instance.service.InstanceManagementService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/instance/management")
public class InstanceManagementController {
    private final InstanceManagementService service;

    @PostMapping("/listPage")
    @SaCheckPermission(WorkflowInstancePermission.LIST)
    public Result<PageData<InstanceListVO>> listPage(@RequestBody @Valid InstanceListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/detail")
    @SaCheckPermission(WorkflowInstancePermission.DETAIL)
    public Result<InstanceAdminDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }
}
