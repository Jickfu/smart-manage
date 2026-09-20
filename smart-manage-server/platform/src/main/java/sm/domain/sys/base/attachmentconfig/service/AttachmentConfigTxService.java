package sm.domain.sys.base.attachmentconfig.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.attachmentconfig.mapper.AttachmentConfigMapper;
import sm.domain.sys.base.attachmentconfig.model.entity.AttachmentConfigEntity;
import sm.domain.sys.base.attachmentconfig.model.form.AttachmentConfigSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.resource.AttachmentUploadPolicy;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class AttachmentConfigTxService {
    private final AttachmentConfigMapper mapper;

    public Long save(AttachmentConfigSaveForm form, AttachmentUploadPolicy policy) {
        AttachmentConfigEntity entity = mapper.selectById(form.getId());
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "附件全局限制配置不存在");
        }
        if (!Objects.equals(entity.getVersion(), form.getVersion())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "附件全局限制配置已被其他用户修改，请刷新后重试");
        }
        entity.setMaxUploadBytes(policy.maxUploadBytes());
        entity.setAllowedExtensions(String.join(",", policy.allowedExtensions()));
        entity.setAllowedMimeTypes(String.join(",", policy.allowedMimeTypes()));
        entity.setTempExpireHours(policy.tempExpireHours());
        if (mapper.updateById(entity) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "附件全局限制配置已被其他用户修改，请刷新后重试");
        }
        return entity.getId();
    }
}
