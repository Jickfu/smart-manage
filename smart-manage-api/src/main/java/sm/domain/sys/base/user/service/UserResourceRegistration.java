package sm.domain.sys.base.user.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.common.helper.CurrentUserContext;
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
    private final CurrentUserContext currentUserContext;

    @Override public String resourceType() { return RESOURCE_TYPE; }
    @Override public BusinessResourceAccessPolicy accessPolicy() { return this; }
    @Override public String objectPrefix() { return "asset/sys/base/user-avatar"; }
    @Override public void requireUploadAllowed() { StpUtil.checkLogin(); }

    @Override
    public void requireAllowed(String resourceId, BusinessResourceAction action) {
        StpUtil.checkLogin();
        try {
            Long userId = Long.valueOf(resourceId);
            if (action != BusinessResourceAction.READ && !userId.equals(currentUserContext.getUserId())) {
                StpUtil.checkPermission(SAVE_PERMISSION);
            }
            if (mapper.selectById(userId) == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
        } catch (NumberFormatException exception) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "用户资源标识无效");
        }
    }
}
