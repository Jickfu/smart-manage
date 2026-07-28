package sm.domain.sys.base.uiconfig.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.uiconfig.model.form.UiConfigListForm;
import sm.domain.sys.base.uiconfig.model.form.UiConfigSaveForm;
import sm.domain.sys.base.uiconfig.model.vo.UiConfigDetailVO;
import sm.domain.sys.base.uiconfig.model.vo.UiConfigListVO;
import sm.domain.sys.base.uiconfig.service.UiConfigService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

/**
 * 界面配置管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "系统建模-界面配置", description = "界面配置管理接口")
@RequiredArgsConstructor
@Slf4j
public class UiConfigController {
    private final UiConfigService service;

    @PostMapping("/sys/base/ui-config/listPage")
    @Operation(summary = "界面配置列表", description = "获取界面配置分页列表")
    @SaCheckPermission("sys:base:ui-config:listPage")
    public Result<PageData<UiConfigListVO>> listPage(@RequestBody UiConfigListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/ui-config/detail")
    @Operation(summary = "界面配置详情", description = "按ID查询界面配置")
    @SaCheckPermission("sys:base:ui-config:detail")
    public Result<UiConfigDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/ui-config/save")
    @Operation(summary = "保存界面配置", description = "新增或更新界面配置")
    @SaCheckPermission("sys:base:ui-config:save")
    public Result<Long> save(@Valid @RequestBody UiConfigSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/base/ui-config/delete")
    @Operation(summary = "删除界面配置", description = "按ID删除界面配置")
    @SaCheckPermission("sys:base:ui-config:delete")
    public Result<String> delete(@RequestBody @Valid IdForm form) {
        service.deleteById(form.getId());
        return Result.success();
    }

    @PostMapping("/sys/base/ui-config/active")
    @Operation(summary = "获取活跃配置", description = "获取当前活跃的界面配置（无需登录）")
    @SaIgnore
    public Result<UiConfigDetailVO> active() {
        return Result.success(service.getActiveConfig());
    }

}
