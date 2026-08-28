package sm.domain.sys.message.inbox.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.message.inbox.model.form.InboxCursorListForm;
import sm.domain.sys.message.inbox.model.form.InboxMarkReadForm;
import sm.domain.sys.message.inbox.model.form.InboxReceiptKeyForm;
import sm.domain.sys.message.inbox.model.vo.InboxCursorPageVO;
import sm.domain.sys.message.inbox.model.vo.InboxDetailVO;
import sm.domain.sys.message.inbox.model.vo.InboxUnreadSummaryVO;
import sm.domain.sys.message.inbox.service.InboxService;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@Tag(name = "个人消息中心", description = "当前登录用户站内消息接口")
public class InboxController {
    private final InboxService service;

    @GetMapping("/sys/message/inbox/unread-summary")
    @Operation(summary = "查询未读角标")
    public Result<InboxUnreadSummaryVO> unreadSummary() {
        return Result.success(service.unreadSummary());
    }

    @PostMapping("/sys/message/inbox/list")
    @Operation(summary = "游标查询当前用户消息")
    public Result<InboxCursorPageVO> list(@RequestBody @Valid InboxCursorListForm form) {
        return Result.success(service.list(form));
    }

    @PostMapping("/sys/message/inbox/detail")
    @Operation(summary = "查询当前用户消息详情")
    public Result<InboxDetailVO> detail(@RequestBody @Valid InboxReceiptKeyForm form) {
        return Result.success(service.detail(form));
    }

    @PostMapping("/sys/message/inbox/mark-read")
    @Operation(summary = "标记已读")
    public Result<String> markRead(@RequestBody @Valid InboxMarkReadForm form) {
        service.markRead(form);
        return Result.success();
    }

    @PostMapping("/sys/message/inbox/mark-unread")
    @Operation(summary = "标记未读")
    public Result<String> markUnread(@RequestBody @Valid InboxMarkReadForm form) {
        service.markUnread(form);
        return Result.success();
    }

    @PostMapping("/sys/message/inbox/mark-all-read")
    @Operation(summary = "全部标记已读")
    public Result<String> markAllRead() {
        service.markAllRead();
        return Result.success();
    }
}
