package sm.domain.sys.base.fileartifact.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import sm.domain.sys.base.fileartifact.service.FileArtifactDownloadClaim;
import sm.domain.sys.base.fileartifact.service.FileArtifactService;
import sm.system.form.IdForm;
import sm.system.storage.FileStorageServiceFactory;

import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class FileArtifactController {
    private final FileArtifactService service;
    private final FileStorageServiceFactory storageFactory;

    @PostMapping("/sys/base/file-artifact/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestBody @Valid IdForm form) {
        FileArtifactDownloadClaim claim = service.claim(form.getId());
        var entity = claim.artifact();
        StreamingResponseBody body = outputStream -> {
            try (var inputStream = storageFactory.getService(entity.getStorageType()).openStream(entity.getObjectKey())) {
                inputStream.transferTo(outputStream);
            } catch (java.io.IOException exception) {
                service.releaseQuietly(claim, exception);
                throw exception;
            } catch (RuntimeException exception) {
                service.releaseQuietly(claim, exception);
                throw exception;
            }
            // 内容已经完整交给响应流后，完成状态失败也不能重新开放一次性凭据。
            service.complete(claim);
        };
        String encodedName = java.net.URLEncoder.encode(entity.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(entity.getMimeType()))
                .contentLength(entity.getFileSize()).header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName).body(body);
    }
}
