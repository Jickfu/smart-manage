package sm.domain.sys.base.uiconfig.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;
import sm.domain.sys.base.uiconfig.constant.UiConfigPermission;
import sm.domain.sys.base.uiconfig.model.form.UiConfigSaveForm;
import sm.domain.sys.base.uiconfig.model.vo.UiConfigDetailVO;
import sm.domain.sys.base.uiconfig.service.UiConfigService;
import sm.system.response.Result;

/**
 * 界面配置管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "系统管理-界面配置", description = "界面配置管理接口")
@RequiredArgsConstructor
@Slf4j
public class UiConfigController {
    private final UiConfigService service;
    private final FileStorageServiceFactory storageFactory;

    @GetMapping("/sys/base/ui-config/singleton")
    @Operation(summary = "获取界面配置")
    @SaCheckPermission(UiConfigPermission.DETAIL)
    public Result<UiConfigDetailVO> singleton() {
        return Result.success(service.singleton());
    }

    @PostMapping("/sys/base/ui-config/save")
    @Operation(summary = "保存界面配置", description = "新增或更新界面配置")
    @SaCheckPermission(UiConfigPermission.SAVE)
    public Result<Long> save(@Valid @RequestBody UiConfigSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/base/ui-config/active")
    @Operation(summary = "获取活跃配置", description = "获取当前活跃的界面配置（无需登录）")
    @SaIgnore
    public Result<UiConfigDetailVO> active() {
        return Result.success(service.getActiveConfig());
    }

    @GetMapping("/sys/base/ui-config/image/{imageType}")
    @Operation(summary = "读取生效界面图片", description = "公开读取当前界面配置绑定的品牌图片")
    @SaIgnore
    public ResponseEntity<StreamingResponseBody> image(@PathVariable String imageType) {
        AttachmentEntity attachment = service.requireActiveImage(imageType);
        FileStorageService storage = storageFactory.getService(attachment.getStorageType());
        StreamingResponseBody body = outputStream -> {
            try (java.io.InputStream inputStream = storage.openStream(attachment.getObjectKey())) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        attachment.getMimeType() != null ? attachment.getMimeType() : "application/octet-stream"))
                .contentLength(attachment.getFileSize())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(body);
    }

}
