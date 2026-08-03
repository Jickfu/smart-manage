package sm.domain.sys.base.attachment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.model.vo.AttachmentVO;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.system.form.IdForm;
import sm.system.exception.BizException;
import sm.system.response.Result;
import sm.system.response.ResultEnum;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 附件管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "附件管理", description = "附件上传/下载/删除接口")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {
    private final AttachmentService service;
    private final FileStorageServiceFactory storageFactory;

    @PostMapping("/sys/base/attachment/upload")
    @Operation(summary = "上传附件", description = "上传文件到临时目录")
    public Result<AttachmentVO> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "bizType", required = false) String bizType) throws IOException {
        return Result.success(service.upload(file, bizType));
    }

    @PostMapping("/sys/base/attachment/promote")
    @Operation(summary = "提升附件", description = "将临时附件关联到业务单据并移出临时目录")
    public Result<String> promote(@Valid @RequestBody AttachmentPromoteForm form) throws IOException {
        service.promote(form);
        return Result.success();
    }

    @PostMapping("/sys/base/attachment/delete")
    @Operation(summary = "删除附件", description = "删除附件及其物理文件")
    public Result<String> delete(@RequestBody @Valid IdForm form) throws IOException {
        service.delete(form.getId());
        return Result.success();
    }

    @PostMapping("/sys/base/attachment/listByBiz")
    @Operation(summary = "按业务查询附件", description = "根据业务类型和单据ID查询附件列表")
    public Result<List<AttachmentVO>> listByBiz(@RequestBody AttachmentPromoteForm form) {
        return Result.success(service.listByBiz(form.getBizType(), form.getBizId()));
    }

    @PostMapping("/sys/base/attachment/listByIds")
    @Operation(summary = "按ID列表查询附件")
    public Result<List<AttachmentVO>> listByIds(@RequestBody List<Long> ids) {
        return Result.success(service.listByIds(ids));
    }

    @PostMapping("/sys/base/attachment/download")
    @Operation(summary = "下载附件", description = "按附件ID下载文件")
    public ResponseEntity<byte[]> download(@RequestBody @Valid IdForm form) throws IOException {
        AttachmentEntity entity = service.requireDownloadableAttachment(form.getId());
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件不存在：" + form.getId());
        }
        FileStorageService storage = storageFactory.getService(entity.getStorageType());
        byte[] bytes = storage.getBytes(entity.getStoredPath());
        String encodedName = URLEncoder.encode(entity.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        entity.getMimeType() != null ? entity.getMimeType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(bytes);
    }
}
