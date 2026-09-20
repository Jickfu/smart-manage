package sm.domain.sys.base.openapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import sm.domain.sys.base.openapi.constant.OpenApiPermission;
import sm.domain.sys.base.openapi.model.form.OpenApiApplicationSaveForm;
import sm.domain.sys.base.openapi.model.form.OpenApiCredentialCreateForm;
import sm.domain.sys.base.openapi.model.form.OpenApiEnableForm;
import sm.domain.sys.base.openapi.model.form.OpenApiListForm;
import sm.domain.sys.base.openapi.model.form.OpenApiReleaseStatusForm;
import sm.domain.sys.base.openapi.model.form.OpenApiCatalogExportForm;
import sm.domain.sys.base.openapi.model.form.OpenApiCatalogTestForm;
import sm.domain.sys.base.openapi.service.OpenApiCatalogTestService;
import sm.domain.sys.base.openapi.service.OpenApiManagementService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sys/base/openapi")
@Tag(name = "系统管理-OpenAPI 平台", description = "第三方应用、API 目录与调用监控")
public class OpenApiManagementController {
    private final OpenApiManagementService service;
    private final OpenApiCatalogTestService testService;

    @PostMapping("/application/listPage")
    @SaCheckPermission(OpenApiPermission.APPLICATION_LIST)
    public Result<PageData<Map<String, Object>>> applicationList(@RequestBody OpenApiListForm form) {
        return Result.success(service.applicationList(form));
    }

    @PostMapping("/application/detail")
    @SaCheckPermission(OpenApiPermission.APPLICATION_DETAIL)
    public Result<Map<String, Object>> applicationDetail(@Valid @RequestBody IdForm form) {
        return Result.success(service.applicationDetail(form.getId()));
    }

    @PostMapping("/application/save")
    @SaCheckPermission(value = {OpenApiPermission.APPLICATION_SAVE,
            OpenApiPermission.APPLICATION_GRANT}, mode = SaMode.AND)
    public Result<Long> applicationSave(@Valid @RequestBody OpenApiApplicationSaveForm form) {
        return Result.success(service.saveApplication(form));
    }

    @PostMapping("/application/enable")
    @SaCheckPermission(OpenApiPermission.APPLICATION_ENABLE)
    public Result<Void> applicationEnable(@Valid @RequestBody OpenApiEnableForm form) {
        service.enableApplication(form.id(), form.version(), form.enabled());
        return Result.success();
    }

    @PostMapping("/credential/list")
    @SaCheckPermission(OpenApiPermission.APPLICATION_DETAIL)
    public Result<List<Map<String, Object>>> credentialList(@Valid @RequestBody IdForm form) {
        return Result.success(service.credentialList(form.getId()));
    }

    @PostMapping("/credential/create")
    @SaCheckPermission(OpenApiPermission.APPLICATION_CREDENTIAL)
    public Result<Map<String, Object>> credentialCreate(
            @Valid @RequestBody OpenApiCredentialCreateForm form) {
        return Result.success(service.createCredential(form));
    }

    @PostMapping("/credential/enable")
    @SaCheckPermission(OpenApiPermission.APPLICATION_CREDENTIAL)
    public Result<Void> credentialEnable(@Valid @RequestBody OpenApiEnableForm form) {
        service.enableCredential(form.id(), form.version(), form.enabled());
        return Result.success();
    }

    @PostMapping("/catalog/listPage")
    @SaCheckPermission(value = {OpenApiPermission.CATALOG_LIST,
            OpenApiPermission.APPLICATION_GRANT}, mode = SaMode.OR)
    public Result<PageData<Map<String, Object>>> catalogList(@RequestBody OpenApiListForm form) {
        return Result.success(service.catalogList(form));
    }

    @GetMapping("/catalog/hierarchy")
    @SaCheckPermission(value = {OpenApiPermission.CATALOG_LIST,
            OpenApiPermission.APPLICATION_GRANT}, mode = SaMode.OR)
    public Result<List<Map<String, Object>>> catalogHierarchy() {
        return Result.success(service.catalogHierarchy());
    }

    @PostMapping("/catalog/detail")
    @SaCheckPermission(OpenApiPermission.CATALOG_LIST)
    public Result<Map<String, Object>> catalogDetail(@Valid @RequestBody IdForm form) {
        return Result.success(service.catalogDetail(form.getId()));
    }

    @PostMapping("/catalog/export")
    @SaCheckPermission(OpenApiPermission.CATALOG_LIST)
    public ResponseEntity<byte[]> catalogExport(@Valid @RequestBody OpenApiCatalogExportForm form) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("api-documentation.md", java.nio.charset.StandardCharsets.UTF_8)
                        .build().toString())
                .body(service.exportCatalog(form.ids()));
    }

    @PostMapping("/catalog/test/applications")
    @SaCheckPermission(OpenApiPermission.CATALOG_TEST)
    public Result<List<Map<String, Object>>> catalogTestApplications(@Valid @RequestBody IdForm form) {
        return Result.success(testService.availableApplications(form.getId()));
    }

    @PostMapping("/catalog/test/execute")
    @SaCheckPermission(OpenApiPermission.CATALOG_TEST)
    public Result<Map<String, Object>> catalogTestExecute(@Valid @RequestBody OpenApiCatalogTestForm form) {
        return Result.success(testService.execute(form));
    }

    @PostMapping("/catalog/status")
    @SaCheckPermission(OpenApiPermission.CATALOG_PUBLISH)
    public Result<Void> catalogStatus(@Valid @RequestBody OpenApiReleaseStatusForm form) {
        service.updateReleaseStatus(form.id(), form.version(), form.status());
        return Result.success();
    }

    @PostMapping("/invocation/listPage")
    @SaCheckPermission(OpenApiPermission.INVOCATION_LIST)
    public Result<PageData<Map<String, Object>>> invocationList(@RequestBody OpenApiListForm form) {
        return Result.success(service.invocationList(form));
    }

    @GetMapping("/invocation/stats")
    @SaCheckPermission(OpenApiPermission.INVOCATION_LIST)
    public Result<Map<String, Object>> invocationStats() {
        return Result.success(service.invocationStats());
    }
}
