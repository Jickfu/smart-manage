package sm.domain.sys.base.attachmentconfig.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.attachmentconfig.constant.AttachmentConfigPermission;
import sm.domain.sys.base.attachmentconfig.model.form.AttachmentConfigSaveForm;
import sm.domain.sys.base.attachmentconfig.model.vo.AttachmentConfigDetailVO;
import sm.domain.sys.base.attachmentconfig.service.AttachmentConfigService;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
public class AttachmentConfigController {
    private final AttachmentConfigService service;

    @GetMapping("/sys/base/attachment-config/singleton")
    @SaCheckPermission(AttachmentConfigPermission.DETAIL)
    public Result<AttachmentConfigDetailVO> singleton() {
        return Result.success(service.singleton());
    }

    @PostMapping("/sys/base/attachment-config/save")
    @SaCheckPermission(AttachmentConfigPermission.SAVE)
    public Result<Long> save(@Valid @RequestBody AttachmentConfigSaveForm form) {
        return Result.success(service.save(form));
    }
}
