package sm.domain.sys.base.uiconfig.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.uiconfig.model.entity.UiConfigEntity;
import sm.domain.sys.base.uiconfig.model.form.UiConfigSaveForm;
import sm.domain.sys.base.uiconfig.mapper.UiConfigMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

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

    /** 新增/编辑，清除缓存 */
    public Long save(UiConfigSaveForm form, Long reservedId) {
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

}
