package sm.domain.sys.base.attachmentconfig.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.attachmentconfig.mapper.AttachmentConfigMapper;
import sm.domain.sys.base.attachmentconfig.model.entity.AttachmentConfigEntity;
import sm.domain.sys.base.attachmentconfig.model.form.AttachmentConfigSaveForm;
import sm.domain.sys.base.attachmentconfig.model.vo.AttachmentConfigDetailVO;
import sm.system.security.authorization.AdministratorOnly;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.resource.AttachmentUploadPolicy;
import sm.system.resource.AttachmentUploadPolicyProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 附件全局限制配置的唯一公开入口。 */
@Service
@RequiredArgsConstructor
public class AttachmentConfigService implements AttachmentUploadPolicyProvider {
    private final AttachmentConfigMapper mapper;
    private final AttachmentConfigTxService txService;

    public AttachmentConfigDetailVO singleton() {
        return toDetail(requireSingleton());
    }

    /** 供附件上传校验读取，禁止使用资源注册类分散定义限制。 */
    public AttachmentUploadPolicy uploadPolicy() {
        AttachmentConfigEntity entity = requireSingleton();
        return new AttachmentUploadPolicy(entity.getMaxUploadBytes(), split(entity.getAllowedExtensions()),
                split(entity.getAllowedMimeTypes()), entity.getTempExpireHours());
    }

    @BizLog("保存附件全局限制配置")
    @AdministratorOnly
    public Long save(AttachmentConfigSaveForm form) {
        AttachmentUploadPolicy policy = normalize(form);
        return txService.save(form, policy);
    }

    private AttachmentConfigEntity requireSingleton() {
        List<AttachmentConfigEntity> entities = mapper.selectList(null);
        if (entities.size() != 1) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "附件全局限制配置不存在或不是单例");
        }
        return entities.getFirst();
    }

    private AttachmentUploadPolicy normalize(AttachmentConfigSaveForm form) {
        List<String> extensions = normalizeValues(form.getAllowedExtensions(), false);
        List<String> mimeTypes = normalizeValues(form.getAllowedMimeTypes(), true);
        if (extensions.stream().anyMatch(value -> !value.matches("[a-z0-9]{1,16}"))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "附件扩展名只能包含小写字母和数字");
        }
        if (mimeTypes.stream().anyMatch(value -> !value.matches("[a-z0-9.+-]+/[a-z0-9.+-]+"))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "附件 MIME 类型格式非法");
        }
        return new AttachmentUploadPolicy(form.getMaxUploadBytes(), extensions, mimeTypes, form.getTempExpireHours());
    }

    private List<String> normalizeValues(List<String> values, boolean mimeType) {
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            String normalizedValue = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
            if (normalizedValue.isBlank() || normalized.contains(normalizedValue)) {
                throw new BizException(ResultEnum.PARAM_ERROR, mimeType ? "MIME 类型不能为空或重复" : "扩展名不能为空或重复");
            }
            normalized.add(normalizedValue);
        }
        return List.copyOf(normalized);
    }

    private AttachmentConfigDetailVO toDetail(AttachmentConfigEntity entity) {
        AttachmentConfigDetailVO detail = new AttachmentConfigDetailVO();
        detail.setId(entity.getId());
        detail.setVersion(entity.getVersion());
        detail.setMaxUploadBytes(entity.getMaxUploadBytes());
        detail.setAllowedExtensions(split(entity.getAllowedExtensions()));
        detail.setAllowedMimeTypes(split(entity.getAllowedMimeTypes()));
        detail.setTempExpireHours(entity.getTempExpireHours());
        return detail;
    }

    private List<String> split(String values) {
        return java.util.Arrays.stream(values.split(","))
                .map(String::trim).filter(value -> !value.isBlank()).toList();
    }
}
