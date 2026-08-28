package sm.domain.sys.message.inbox.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.message.inbox.constant.InboxMessagePermission;
import sm.domain.sys.message.inbox.model.form.InboxMessageListForm;
import sm.domain.sys.message.inbox.model.form.InboxMessageSaveForm;
import sm.domain.sys.message.inbox.model.form.InboxMessageVersionForm;
import sm.domain.sys.message.inbox.model.vo.InboxMessageCreateNewDataVO;
import sm.domain.sys.message.inbox.model.vo.InboxMessageDetailVO;
import sm.domain.sys.message.inbox.model.vo.InboxMessageListVO;
import sm.domain.sys.message.inbox.service.InboxMessageAdminService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统管理-站内消息", description = "管理员全站站内消息接口")
public class InboxMessageAdminController {
    private final InboxMessageAdminService service;

    @PostMapping("/sys/message/inbox/admin/listPage")
    @SaCheckPermission(InboxMessagePermission.LIST)
    @Operation(summary = "站内消息管理列表")
    public Result<PageData<InboxMessageListVO>> listPage(@RequestBody InboxMessageListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/message/inbox/admin/detail")
    @SaCheckPermission(InboxMessagePermission.DETAIL)
    @Operation(summary = "站内消息管理详情")
    public Result<InboxMessageDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @GetMapping("/sys/message/inbox/admin/createNewData")
    @SaCheckPermission(InboxMessagePermission.SAVE)
    @Operation(summary = "获取站内消息新增默认值")
    public Result<InboxMessageCreateNewDataVO> createNewData() {
        return Result.success(service.createNewData());
    }

    @PostMapping("/sys/message/inbox/admin/save")
    @SaCheckPermission(InboxMessagePermission.SAVE)
    @Operation(summary = "保存站内消息草稿")
    public Result<Long> save(@RequestBody @Valid InboxMessageSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/message/inbox/admin/publish")
    @SaCheckPermission(InboxMessagePermission.PUBLISH)
    @Operation(summary = "发布全站站内消息")
    public Result<String> publish(@RequestBody @Valid InboxMessageVersionForm form) {
        service.publish(form);
        return Result.success();
    }

    @PostMapping("/sys/message/inbox/admin/retry")
    @SaCheckPermission(InboxMessagePermission.RETRY)
    @Operation(summary = "重试发布站内消息")
    public Result<String> retry(@RequestBody @Valid InboxMessageVersionForm form) {
        service.retry(form);
        return Result.success();
    }
}
