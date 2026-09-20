package sm.domain.sys.monitor.script.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.script.constant.ScriptPermission;
import sm.domain.sys.monitor.script.model.form.*;
import sm.domain.sys.monitor.script.model.vo.*;
import sm.domain.sys.monitor.script.service.ScriptService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@Tag(name = "脚本控制台", description = "GraalJS 运维脚本执行、管理与审计")
@RequiredArgsConstructor
public class ScriptController {
    private final ScriptService scriptService;

    @PostMapping("/sys/monitor/script/execute")
    @Operation(summary = "执行 JavaScript")
    @SaCheckPermission(ScriptPermission.EXECUTE)
    public Result<ScriptResultVO> execute(@Valid @RequestBody ScriptExecuteForm form) {
        return Result.success(scriptService.execute(form));
    }

    @PostMapping("/sys/monitor/script/listPage")
    @Operation(summary = "脚本分页查询")
    @SaCheckPermission(ScriptPermission.LIST)
    public Result<PageData<ScriptListVO>> listPage(@Valid @RequestBody ScriptListForm form) {
        return Result.success(scriptService.listPage(form));
    }

    @PostMapping("/sys/monitor/script/detail")
    @Operation(summary = "脚本详情")
    @SaCheckPermission(ScriptPermission.DETAIL)
    public Result<ScriptDetailVO> detail(@Valid @RequestBody IdForm form) {
        return Result.success(scriptService.detail(form.getId()));
    }

    @GetMapping("/sys/monitor/script/createNewData")
    @Operation(summary = "脚本新增默认值")
    @SaCheckPermission(ScriptPermission.SAVE)
    public Result<ScriptDetailVO> createNewData() {
        return Result.success(scriptService.createNewData());
    }

    @GetMapping("/sys/monitor/script/apiMetadata")
    @Operation(summary = "可调用领域 Service API 元数据")
    @SaCheckPermission(ScriptPermission.EXECUTE)
    public Result<java.util.List<ScriptApiServiceVO>> apiMetadata() {
        return Result.success(scriptService.apiMetadata());
    }

    @PostMapping("/sys/monitor/script/save")
    @Operation(summary = "保存脚本")
    @SaCheckPermission(ScriptPermission.SAVE)
    public Result<Long> save(@Valid @RequestBody ScriptSaveForm form) {
        return Result.success(scriptService.save(form));
    }

    @PostMapping("/sys/monitor/script/delete")
    @Operation(summary = "删除脚本")
    @SaCheckPermission(ScriptPermission.DELETE)
    public Result<String> delete(@Valid @RequestBody ScriptDeleteForm form) {
        scriptService.delete(form);
        return Result.success();
    }

    @PostMapping("/sys/monitor/script/log/listPage")
    @Operation(summary = "脚本执行历史分页查询")
    @SaCheckPermission(ScriptPermission.LOG_LIST)
    public Result<PageData<ScriptLogListVO>> logListPage(@Valid @RequestBody ScriptLogListForm form) {
        return Result.success(scriptService.logListPage(form));
    }

    @PostMapping("/sys/monitor/script/log/detail")
    @Operation(summary = "脚本执行历史详情")
    @SaCheckPermission(ScriptPermission.LOG_DETAIL)
    public Result<ScriptLogDetailVO> logDetail(@Valid @RequestBody IdForm form) {
        return Result.success(scriptService.logDetail(form.getId()));
    }
}
