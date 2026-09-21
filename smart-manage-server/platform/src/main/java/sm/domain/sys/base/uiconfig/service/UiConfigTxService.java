package sm.domain.sys.base.uiconfig.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.attachment.contract.AttachmentGateway;
import sm.domain.sys.base.attachment.contract.AttachmentPromoteCommand;
import sm.domain.sys.base.uiconfig.model.entity.UiConfigEntity;
import sm.domain.sys.base.uiconfig.model.form.UiConfigSaveForm;
import sm.domain.sys.base.uiconfig.mapper.UiConfigMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * 界面配置事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class UiConfigTxService {
    private final UiConfigMapper mapper;
    private final AttachmentGateway attachmentGateway;

    /** 新增/编辑，清除缓存 */
    public Long save(UiConfigSaveForm form, Long reservedId) {
        promoteImages(form, reservedId);
        UiConfigEntity entity;
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "界面配置不存在");
            }
            if (form.getVersion() == null || !Objects.equals(entity.getVersion(), form.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "界面配置已被其他用户修改，请刷新后重试");
            }
        } else {
            if (mapper.selectCount(null) > 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "界面配置为单例，不能重复新增");
            }
            entity = new UiConfigEntity();
            entity.setId(reservedId);
        }
        entity.setPageTitle(form.getPageTitle());
        entity.setSystemName(form.getSystemName());
        entity.setLoginBanner(form.getLoginBanner());
        entity.setLoginLogo(form.getLoginLogo());
        entity.setHeaderLogo(form.getHeaderLogo());
        entity.setLoginBannerAttachmentId(form.getLoginBannerAttachmentId());
        entity.setLoginLogoAttachmentId(form.getLoginLogoAttachmentId());
        entity.setHeaderLogoAttachmentId(form.getHeaderLogoAttachmentId());
        entity.setWatermarkEnabled(form.getWatermarkEnabled());
        entity.setWatermarkContent(normalizeWatermarkContent(form.getWatermarkContent()));
        entity.setWatermarkShowName(form.getWatermarkShowName());
        entity.setWatermarkShowPhone(form.getWatermarkShowPhone());
        entity.setWatermarkShowEmail(form.getWatermarkShowEmail());
        entity.setWatermarkShowNumber(form.getWatermarkShowNumber());
        entity.setWatermarkShowRootOrg(form.getWatermarkShowRootOrg());
        entity.setWatermarkGapX(form.getWatermarkGapX());
        entity.setWatermarkGapY(form.getWatermarkGapY());
        entity.setWatermarkFontSize(form.getWatermarkFontSize());
        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            if (mapper.updateById(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.DATA_CONFLICT, "数据已被其他用户修改");
            }
        }
        return entity.getId();
    }

    /** 临时图片确认必须参与界面配置写事务，任一数据库写入失败时整体回滚。 */
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
            attachmentGateway.promoteForAggregate(promoteCommand);
        } catch (IOException exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "界面图片确认失败: " + exception.getMessage());
        }
    }

    private String normalizeWatermarkContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        return content.trim();
    }

}
