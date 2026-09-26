package sm.domain.workflow.process.definition.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.workflow.process.definition.constant.WorkflowDefinitionPermission;
import sm.domain.workflow.process.definition.model.form.*;
import sm.domain.workflow.process.definition.model.vo.*;
import sm.domain.workflow.process.definition.service.DefinitionService;
import sm.system.form.IdForm;
import sm.system.response.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflow/process/definition")
public class DefinitionController {
    private final DefinitionService service;
    @PostMapping("/listPage")
    @SaCheckPermission(WorkflowDefinitionPermission.LIST)
    public Result<PageData<DefinitionListVO>> listPage(@RequestBody DefinitionListForm form) { return Result.success(service.listPage(form)); }
    @GetMapping("/versions/{id}")
    @SaCheckPermission(WorkflowDefinitionPermission.LIST)
    public Result<DefinitionListVO> versions(@PathVariable Long id) { return Result.success(service.versions(id)); }
    @GetMapping("/business-types")
    @SaCheckPermission(WorkflowDefinitionPermission.LIST)
    public Result<List<WorkflowBusinessChoiceVO>> businessTypes() { return Result.success(service.businessTypes()); }
    @GetMapping("/design/{id}")
    @SaCheckPermission(WorkflowDefinitionPermission.DESIGN)
    public Result<DefinitionDesignVO> design(@PathVariable Long id) { return Result.success(service.design(id)); }
    @PostMapping("/create")
    @SaCheckPermission(WorkflowDefinitionPermission.SAVE)
    public Result<Long> create(@RequestBody @Valid DefinitionCreateForm form) { return Result.success(service.create(form)); }
    @PostMapping("/design/save")
    @SaCheckPermission(WorkflowDefinitionPermission.SAVE)
    public Result<DefinitionDesignVO> save(@RequestBody @Valid DefinitionSaveForm form) { return Result.success(service.save(form)); }
    @PostMapping("/publish")
    @SaCheckPermission(WorkflowDefinitionPermission.PUBLISH)
    public Result<String> publish(@RequestBody @Valid DefinitionPublishForm form) { service.publish(form); return Result.success(); }
    @PostMapping("/copy")
    @SaCheckPermission(WorkflowDefinitionPermission.SAVE)
    public Result<Long> copy(@RequestBody @Valid IdForm form) { return Result.success(service.copy(form.getId())); }
    @PostMapping("/enabled")
    @SaCheckPermission(WorkflowDefinitionPermission.PUBLISH)
    public Result<String> enabled(@RequestBody @Valid DefinitionEnabledForm form) { service.enabled(form); return Result.success(); }
}
