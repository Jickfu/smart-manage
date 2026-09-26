package sm.domain.demo.office.leave.controller;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.demo.office.leave.constant.LeavePermission;
import sm.domain.demo.office.leave.model.form.*;
import sm.domain.demo.office.leave.model.vo.LeaveDetailVO;
import sm.domain.demo.office.leave.service.LeaveService;
import sm.system.form.IdForm;
import sm.system.form.PageForm;
import sm.system.response.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/demo/office/leave")
public class LeaveController {
    private final LeaveService service;
    @PostMapping("/listPage")
    @SaCheckPermission(LeavePermission.LIST)
    public Result<PageData<LeaveDetailVO>> listPage(@RequestBody PageForm form) { return Result.success(service.listPage(form)); }
    @PostMapping("/detail")
    @SaCheckPermission(LeavePermission.DETAIL)
    public Result<LeaveDetailVO> detail(@RequestBody @Valid IdForm form) { return Result.success(service.detail(form.getId())); }
    /** 审批详情按对应轮次参与资格授权，不扩大普通业务列表的组织范围。 */
    @PostMapping("/approval-detail")
    public Result<LeaveDetailVO> approvalDetail(@RequestBody @Valid LeaveApprovalDetailForm form) { return Result.success(service.approvalDetail(form)); }
    @PostMapping("/save")
    @SaCheckPermission(LeavePermission.SAVE)
    public Result<Long> save(@RequestBody @Valid LeaveSaveForm form) { return Result.success(service.save(form)); }
    @PostMapping("/submit")
    @SaCheckPermission(LeavePermission.SUBMIT)
    public Result<Long> submit(@RequestBody @Valid LeaveSubmitForm form) { return Result.success(service.submit(form)); }
    @PostMapping("/delete")
    @SaCheckPermission(LeavePermission.DELETE)
    public Result<String> delete(@RequestBody @Valid LeaveDeleteForm form) { service.delete(form); return Result.success(); }
}
