package sm.domain.sys.message.email.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.sys.message.email.constant.EmailPermission;
import sm.domain.sys.message.email.model.form.*;
import sm.domain.sys.message.email.service.EmailService;
import sm.system.response.PageData;
import sm.system.response.Result;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统服务-邮件", description = "SMTP 发信账号、正式邮件投递和发送记录")
@RequestMapping("/sys/message/email")
public class EmailController {
    private final EmailService service;

    @PostMapping("/account/listPage") @SaCheckPermission(EmailPermission.ACCOUNT_LIST)
    public Result<PageData<Map<String, Object>>> accountList(@Valid @RequestBody AccountListForm form) { return Result.success(service.accountList(form)); }
    @PostMapping("/account/detail") @SaCheckPermission(EmailPermission.ACCOUNT_DETAIL)
    public Result<Map<String, Object>> accountDetail(@Valid @RequestBody IdForm form) { return Result.success(service.accountDetail(form.id())); }
    @PostMapping("/account/save") @SaCheckPermission(EmailPermission.ACCOUNT_SAVE)
    public Result<Long> accountSave(@Valid @RequestBody AccountSaveForm form) { return Result.success(service.saveAccount(form)); }
    @PostMapping("/account/enable") @SaCheckPermission(EmailPermission.ACCOUNT_ENABLE)
    public Result<Void> accountEnable(@Valid @RequestBody AccountEnableForm form) { service.enableAccount(form); return Result.success(); }
    @PostMapping("/account/delete") @SaCheckPermission(EmailPermission.ACCOUNT_DELETE)
    public Result<Void> accountDelete(@Valid @RequestBody VersionForm form) { service.deleteAccount(form.id(), form.version()); return Result.success(); }
    @GetMapping("/account/manual-options") @SaCheckPermission(EmailPermission.COMPOSE_SEND)
    public Result<List<Map<String, Object>>> manualOptions() { return Result.success(service.manualAccountOptions()); }
    @PostMapping("/account/test") @SaCheckPermission(EmailPermission.ACCOUNT_TEST)
    public Result<String> accountTest(@Valid @RequestBody AccountTestForm form) { return Result.success(service.testAccount(form)); }

    @PostMapping("/compose/send") @SaCheckPermission(EmailPermission.COMPOSE_SEND)
    public Result<Long> send(@Valid @RequestBody ComposeForm form) { return Result.success(service.compose(form)); }
    @PostMapping("/record/listPage") @SaCheckPermission(EmailPermission.RECORD_LIST)
    public Result<PageData<Map<String, Object>>> recordList(@Valid @RequestBody RecordListForm form) { return Result.success(service.recordList(form)); }
    @PostMapping("/record/detail") @SaCheckPermission(EmailPermission.RECORD_DETAIL)
    public Result<Map<String, Object>> recordDetail(@Valid @RequestBody IdForm form) { return Result.success(service.recordDetail(form.id())); }
    @PostMapping("/record/retry") @SaCheckPermission(EmailPermission.RECORD_RETRY)
    public Result<Long> retry(@Valid @RequestBody IdForm form) { return Result.success(service.retry(form.id())); }
    @PostMapping("/record/cancel") @SaCheckPermission(EmailPermission.RECORD_CANCEL)
    public Result<Void> cancel(@Valid @RequestBody VersionForm form) { service.cancel(form.id(), form.version()); return Result.success(); }

}
