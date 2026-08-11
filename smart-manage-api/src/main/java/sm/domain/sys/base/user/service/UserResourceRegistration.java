package sm.domain.sys.base.user.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.system.exception.BizException;
import sm.system.resource.BusinessResourceAccessPolicy;
import sm.system.resource.BusinessResourceAction;
import sm.system.resource.BusinessResourceRegistration;
import sm.system.response.ResultEnum;

/** 用户头像附件资源注册。 */
@Component
@RequiredArgsConstructor
final class UserResourceRegistration implements BusinessResourceRegistration, BusinessResourceAccessPolicy {
    static final String RESOURCE_TYPE = "sys.base.user-avatar";
    private static final String SAVE_PERMISSION = "sys:base:user:save";
    private final UserMapper mapper;

    @Override public String resourceType() { return RESOURCE_TYPE; }
    @Override public BusinessResourceAccessPolicy accessPolicy() { return this; }
    @Override public String objectPrefix() { return "asset/sys/base/user-avatar"; }
    @Override public void requireUploadAllowed() { StpUtil.checkPermission(SAVE_PERMISSION); }

    @Override
    public void requireAllowed(String resourceId, BusinessResourceAction action) {
        StpUtil.checkLogin();
        if (action != BusinessResourceAction.READ) StpUtil.checkPermission(SAVE_PERMISSION);
        try {
            if (mapper.selectById(Long.valueOf(resourceId)) == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        } catch (NumberFormatException exception) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "用户资源标识无效");
        }
    }
}
