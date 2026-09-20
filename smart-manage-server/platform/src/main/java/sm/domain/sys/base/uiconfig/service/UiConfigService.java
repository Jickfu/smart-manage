package sm.domain.sys.base.uiconfig.service;

import sm.domain.sys.base.uiconfig.converter.UiConfigConverter;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.uiconfig.model.entity.UiConfigEntity;
import sm.domain.sys.base.uiconfig.model.form.UiConfigSaveForm;
import sm.domain.sys.base.uiconfig.model.vo.UiConfigDetailVO;
import sm.domain.sys.base.uiconfig.mapper.UiConfigMapper;
import sm.domain.sys.base.attachment.contract.AttachmentPromoteCommand;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.contract.AttachmentReference;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.ResultEnum;

import java.util.List;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 界面配置服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UiConfigService {
    private static final String LOGIN_BANNER_IMAGE = "login-banner";
    private static final String LOGIN_LOGO_IMAGE = "login-logo";
    private static final String HEADER_LOGO_IMAGE = "header-logo";
    private static final String PUBLIC_IMAGE_PATH = "/sys/base/ui-config/image/";
    private final UiConfigMapper mapper;
    private final UiConfigTxService txService;
    private final UiConfigConverter converter;
    private final AttachmentService attachmentService;

    /** 单例管理页读取；尚未配置时返回可直接编辑的空对象。 */
    public UiConfigDetailVO singleton() {
        List<UiConfigEntity> entityList = mapper.selectList(null);
        if (entityList.isEmpty()) {
            return new UiConfigDetailVO();
        }
        return assembleDetailVO(entityList.get(0));
    }

    /** 获取活跃配置（Caffeine 本地缓存） */
    public UiConfigDetailVO getActiveConfig() {
        List<UiConfigEntity> entityList = mapper.selectList(null);
        return entityList.isEmpty() ? new UiConfigDetailVO() : assembleDetailVO(entityList.get(0));
    }

    /** 公开读取当前生效的品牌图片；只允许按固定图片类型访问当前配置绑定的正式附件。 */
    public AttachmentEntity requireActiveImage(String imageType) {
        List<UiConfigEntity> entityList = mapper.selectList(null);
        if (entityList.isEmpty()) {
            throw new BizException(ResultEnum.NOT_FOUND, "界面配置不存在");
        }
        UiConfigEntity config = entityList.get(0);
        Long attachmentId = switch (imageType) {
            case LOGIN_BANNER_IMAGE -> config.getLoginBannerAttachmentId();
            case LOGIN_LOGO_IMAGE -> config.getLoginLogoAttachmentId();
            case HEADER_LOGO_IMAGE -> config.getHeaderLogoAttachmentId();
            default -> throw new BizException(ResultEnum.PARAM_ERROR, "不支持的界面图片类型");
        };
        if (attachmentId == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "界面图片未配置");
        }
        return attachmentService.requireAggregateAttachment(
                attachmentId, UiConfigResourceRegistration.RESOURCE_TYPE, String.valueOf(config.getId()));
    }

    @BizLog("保存界面配置")
    public Long save(UiConfigSaveForm form) {
        UiConfigEntity previous = form.getId() == null ? null : mapper.selectById(form.getId());
        Long configId = previous == null ? IdWorker.getId() : previous.getId();
        List<Long> temporaryImageIds = findTemporaryImageIds(form);
        promoteImages(form, configId);
        try {
            txService.save(form, configId);
        } catch (RuntimeException exception) {
            deleteImagesForCompensation(temporaryImageIds);
            throw exception;
        }
        deleteReplacedImages(previous, form);
        return configId;
    }

    /**
     * 图片上传先进入临时目录，配置保存取得真实 ID 后再提升为正式附件。
     * 外部存储与数据库无法原子提交；附件模块负责提升失败时的文件反向移动补偿。
     */
    private void promoteImages(UiConfigSaveForm form, Long configId) {
        LinkedHashSet<Long> attachmentIds = new LinkedHashSet<>();
        if (form.getLoginBannerAttachmentId() != null) {
            attachmentIds.add(form.getLoginBannerAttachmentId());
        }
        if (form.getLoginLogoAttachmentId() != null) {
            attachmentIds.add(form.getLoginLogoAttachmentId());
        }
        if (form.getHeaderLogoAttachmentId() != null) {
            attachmentIds.add(form.getHeaderLogoAttachmentId());
        }
        if (attachmentIds.isEmpty()) {
            return;
        }
        AttachmentPromoteCommand promoteCommand = new AttachmentPromoteCommand();
        promoteCommand.setAttachmentIds(List.copyOf(attachmentIds));
        promoteCommand.setBizType(UiConfigResourceRegistration.RESOURCE_TYPE);
        promoteCommand.setBizId(String.valueOf(configId));
        promoteCommand.setUploadSessions(form.getAttachmentUploadSessions());
        try {
            attachmentService.promoteForAggregate(promoteCommand);
        } catch (IOException exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "界面图片确认失败: " + exception.getMessage());
        }
    }

    private List<Long> findTemporaryImageIds(UiConfigSaveForm form) {
        List<Long> attachmentIds = java.util.stream.Stream.of(
                        form.getLoginBannerAttachmentId(),
                        form.getLoginLogoAttachmentId(),
                        form.getHeaderLogoAttachmentId())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        return attachmentService.listByIds(attachmentIds).stream()
                .filter(attachment -> Boolean.TRUE.equals(attachment.getIsTemp()))
                .map(AttachmentReference::getId)
                .toList();
    }

    private void deleteImagesForCompensation(List<Long> attachmentIds) {
        for (Long attachmentId : attachmentIds) {
            try {
                attachmentService.deleteForAggregate(attachmentId);
            } catch (IOException | RuntimeException cleanupException) {
                log.error("界面配置保存失败且新图片补偿删除失败: id={}", attachmentId, cleanupException);
            }
        }
    }

    /** 图片访问地址依赖当前存储实现，读取时按附件 ID 动态解析，禁止持久化临时 URL。 */
    private UiConfigDetailVO assembleDetailVO(UiConfigEntity entity) {
        UiConfigDetailVO detail = converter.toDetailVO(entity);
        List<Long> attachmentIds = java.util.stream.Stream.of(
                        entity.getLoginBannerAttachmentId(),
                        entity.getLoginLogoAttachmentId(),
                        entity.getHeaderLogoAttachmentId())
                .filter(java.util.Objects::nonNull)
                .toList();
        Map<Long, AttachmentReference> attachmentMap = attachmentService.listByIds(attachmentIds).stream()
                .collect(Collectors.toMap(AttachmentReference::getId, Function.identity()));
        if (entity.getLoginBannerAttachmentId() != null) {
            Long attachmentId = entity.getLoginBannerAttachmentId();
            detail.setLoginBanner(resolveUrl(attachmentMap, attachmentId, LOGIN_BANNER_IMAGE));
        }
        if (entity.getLoginLogoAttachmentId() != null) {
            Long attachmentId = entity.getLoginLogoAttachmentId();
            detail.setLoginLogo(resolveUrl(attachmentMap, attachmentId, LOGIN_LOGO_IMAGE));
        }
        if (entity.getHeaderLogoAttachmentId() != null) {
            Long attachmentId = entity.getHeaderLogoAttachmentId();
            detail.setHeaderLogo(resolveUrl(attachmentMap, attachmentId, HEADER_LOGO_IMAGE));
        }
        return detail;
    }

    private String resolveUrl(Map<Long, AttachmentReference> attachmentMap, Long attachmentId, String imageType) {
        AttachmentReference attachment = attachmentMap.get(attachmentId);
        return attachment == null ? null : PUBLIC_IMAGE_PATH + imageType + "?v=" + attachmentId;
    }

    /** 配置已成功切换后清理被替换的旧附件；清理失败保留告警，不回滚已生效配置。 */
    private void deleteReplacedImages(UiConfigEntity previous, UiConfigSaveForm form) {
        if (previous == null) {
            return;
        }
        LinkedHashSet<Long> previousIds = new LinkedHashSet<>(java.util.stream.Stream.of(
                        previous.getLoginBannerAttachmentId(),
                        previous.getLoginLogoAttachmentId(),
                        previous.getHeaderLogoAttachmentId())
                .filter(java.util.Objects::nonNull)
                .toList());
        previousIds.removeAll(java.util.stream.Stream.of(
                        form.getLoginBannerAttachmentId(),
                        form.getLoginLogoAttachmentId(),
                        form.getHeaderLogoAttachmentId())
                .filter(java.util.Objects::nonNull)
                .toList());
        for (Long attachmentId : previousIds) {
            try {
                attachmentService.deleteForAggregate(attachmentId);
            } catch (IOException | RuntimeException exception) {
                log.warn("界面配置旧图片清理失败，需按附件ID重试: id={}", attachmentId, exception);
            }
        }
    }
}
