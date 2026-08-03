package sm.domain.sys.base.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.user.service.CachedUserProvider;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 当前登录用户资料与真实管理员身份校验。 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {
	private final CurrentUserContext currentUserContext;
	private final CachedUserProvider cachedUserProvider;

	public UserEntity requireCurrentUser() {
		return cachedUserProvider.requireUser(currentUserContext.getUserId());
	}

	public boolean isAdministrator() {
		return currentUserContext.isLogin()
				&& UserConstant.SUPER_ADMIN.equals(requireCurrentUser().getUsername());
	}

	/** 高风险能力必须校验真实账号身份，不能只依赖可配置的权限码。 */
	public void checkAdministrator() {
		if (!isAdministrator()) {
			throw new BizException(ResultEnum.PERMISSION_ERROR, "仅超级管理员可使用此功能");
		}
	}
}
