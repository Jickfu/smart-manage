package sm.domain.sys.base.fileconfig.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.fileconfig.constant.FileConfigPermission;
import sm.domain.sys.base.fileconfig.model.form.FileConfigSaveForm;
import sm.domain.sys.base.fileconfig.model.form.FtpTestForm;
import sm.domain.sys.base.fileconfig.model.vo.FileConfigDetailVO;
import sm.domain.sys.base.fileconfig.service.FileConfigService;
import sm.system.response.Result;

/**
 * 文件配置管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "系统管理-存储配置", description = "文件存储配置管理接口")
@RequiredArgsConstructor
public class FileConfigController {
    private final FileConfigService service;

    @GetMapping("/sys/base/file-config/singleton")
    @Operation(summary = "获取文件配置")
    @SaCheckPermission(FileConfigPermission.DETAIL)
    public Result<FileConfigDetailVO> singleton() {
        return Result.success(service.singleton());
    }

    @PostMapping("/sys/base/file-config/save")
    @Operation(summary = "保存文件配置")
    @SaCheckPermission(FileConfigPermission.SAVE)
    public Result<Long> save(@Valid @RequestBody FileConfigSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/base/file-config/test-ftp")
    @Operation(summary = "测试FTP连接", description = "使用表单中的FTP参数测试连接是否正常")
    @SaCheckPermission(FileConfigPermission.SAVE)
    public Result<String> testFtp(@Valid @RequestBody FtpTestForm form) {
        return Result.success(service.testFtp(form));
    }
}
