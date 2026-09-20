package sm.domain.sys.base.uiconfig.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.uiconfig.mapper.UiConfigMapper;
import sm.system.exception.BizException;
import sm.system.resource.BusinessResourceAccessPolicy;
import sm.system.resource.BusinessResourceAction;
import sm.system.resource.BusinessResourceRegistration;
import sm.system.response.ResultEnum;

/** 界面配置资源注册；已生效的界面图片允许登录页公开读取，修改仍要求保存权限。 */
@Component
@RequiredArgsConstructor
final class UiConfigResourceRegistration implements BusinessResourceRegistration, BusinessResourceAccessPolicy {
    static final String RESOURCE_TYPE = "sys.base.ui-config";
    private static final String SAVE_PERMISSION = "sys:base:ui-config:save";

    private final UiConfigMapper mapper;

    @Override
    public String resourceType() {
        return RESOURCE_TYPE;
    }

    @Override
    public BusinessResourceAccessPolicy accessPolicy() {
        return this;
    }

    @Override
    public String objectPrefix() {
        return "asset/sys/base/ui-config";
    }

    @Override
    public void requireUploadAllowed() {
        StpUtil.checkPermission(SAVE_PERMISSION);
    }

    @Override
    public void requireAllowed(String resourceId, BusinessResourceAction action) {
        if (action != BusinessResourceAction.READ) {
            StpUtil.checkPermission(SAVE_PERMISSION);
        }
        Long configId;
        try {
            configId = Long.valueOf(resourceId);
        } catch (NumberFormatException exception) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "界面配置资源标识非法");
        }
        if (mapper.selectById(configId) == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "界面配置不存在");
        }
    }
}
