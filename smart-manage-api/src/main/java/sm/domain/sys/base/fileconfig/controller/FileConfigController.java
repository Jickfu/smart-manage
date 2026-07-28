package sm.domain.sys.base.fileconfig.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.fileconfig.model.form.FileConfigListForm;
import sm.domain.sys.base.fileconfig.model.form.FileConfigSaveForm;
import sm.domain.sys.base.fileconfig.model.form.FtpTestForm;
import sm.domain.sys.base.fileconfig.model.vo.FileConfigDetailVO;
import sm.domain.sys.base.fileconfig.service.FileConfigService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

/**
 * 文件配置管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "系统建模-文件配置", description = "文件配置管理接口")
@RequiredArgsConstructor
public class FileConfigController {
    private final FileConfigService service;

    @PostMapping("/sys/base/file-config/listPage")
    @Operation(summary = "文件配置列表")
    @SaCheckPermission("sys:base:file-config:listPage")
    public Result<PageData<FileConfigDetailVO>> listPage(@RequestBody FileConfigListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/file-config/detail")
    @Operation(summary = "文件配置详情")
    @SaCheckPermission("sys:base:file-config:detail")
    public Result<FileConfigDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/file-config/save")
    @Operation(summary = "保存文件配置")
    @SaCheckPermission("sys:base:file-config:save")
    public Result<Long> save(@Valid @RequestBody FileConfigSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/base/file-config/delete")
    @Operation(summary = "删除文件配置")
    @SaCheckPermission("sys:base:file-config:delete")
    public Result<String> delete(@RequestBody @Valid IdForm form) {
        service.deleteById(form.getId());
        return Result.success();
    }

    @PostMapping("/sys/base/file-config/test-ftp")
    @Operation(summary = "测试FTP连接", description = "使用表单中的FTP参数测试连接是否正常")
    @SaCheckPermission("sys:base:file-config:save")
    public Result<String> testFtp(@Valid @RequestBody FtpTestForm form) {
        return Result.success(service.testFtp(form));
    }
}
