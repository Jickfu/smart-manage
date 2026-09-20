package sm.domain.sys.base.sysparam.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.sysparam.constant.SysParamPermission;
import sm.domain.sys.base.sysparam.model.form.SysParamListForm;
import sm.domain.sys.base.sysparam.model.form.SysParamSaveForm;
import sm.domain.sys.base.sysparam.model.vo.SysParamCreateNewDataVO;
import sm.domain.sys.base.sysparam.model.vo.SysParamVO;
import sm.domain.sys.base.sysparam.model.vo.SysParamDetailVO;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

/**
 * 系统参数管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "系统管理-系统参数", description = "系统参数管理接口")
@RequiredArgsConstructor
@Slf4j
public class SysParamController {
    private final SysParamService service;

    @PostMapping("/sys/base/param/listPage")
    @Operation(summary = "系统参数列表")
    @SaCheckPermission(SysParamPermission.LIST)
    public Result<PageData<SysParamVO>> listPage(@RequestBody SysParamListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/param/detail")
    @Operation(summary = "系统参数详情")
    @SaCheckPermission(SysParamPermission.DETAIL)
    public Result<SysParamDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/param/save")
    @Operation(summary = "保存系统参数")
    @SaCheckPermission(SysParamPermission.SAVE)
    public Result<Long> save(@Valid @RequestBody SysParamSaveForm form) {
        return Result.success(service.save(form));
    }

    @GetMapping("/sys/base/param/createNewData")
    @Operation(summary = "获取新增默认值")
    @SaCheckPermission(SysParamPermission.SAVE)
    public Result<SysParamCreateNewDataVO> createNewData() {
        return Result.success(service.createNewData());
    }

    @PostMapping("/sys/base/param/delete")
    @Operation(summary = "删除系统参数")
    @SaCheckPermission(SysParamPermission.DELETE)
    public Result<String> delete(@RequestBody @Valid IdForm form) {
        service.deleteById(form.getId());
        return Result.success();
    }
}
