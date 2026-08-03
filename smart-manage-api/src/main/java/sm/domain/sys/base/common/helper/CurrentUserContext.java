package sm.domain.sys.base.common.helper;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.config.OrgConfig;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 当前登录用户的 Sa-Token 会话上下文。 */
@Component
@RequiredArgsConstructor
public class CurrentUserContext {
	private static final String ORG_ID_KEY = "orgId";
	private static final String USERNAME_KEY = "username";
	private static final String ADMINISTRATOR_KEY = "administrator";
	private final OrgConfig orgConfig;

	/** 凭据验证并建立正式登录态后，集中初始化服务端认证声明。 */
	public void initializeIdentity(String username, boolean administrator) {
		var session = StpUtil.getTokenSession();
		session.set(ORG_ID_KEY, orgConfig.getDefaultId());
		session.set(USERNAME_KEY, username);
		session.set(ADMINISTRATOR_KEY, administrator);
	}

	public Long getUserId() {
		return StpUtil.getLoginIdAsLong();
	}

	public Long getOrgId() {
		Long orgId = StpUtil.getTokenSession().getLong(ORG_ID_KEY);
		return orgId != null ? orgId : orgConfig.getDefaultId();
	}

	public void setOrgId(Long orgId) {
		StpUtil.getTokenSession().set(ORG_ID_KEY, orgId);
	}

	public String getUsernameOrDefault(String defaultUsername) {
		String username = StpUtil.getTokenSession().getString(USERNAME_KEY);
		return username != null ? username : defaultUsername;
	}

	public boolean isAdministrator() {
		return isLogin() && Boolean.TRUE.equals(
				StpUtil.getTokenSession().get(ADMINISTRATOR_KEY));
	}

	/** 高风险能力必须校验登录时由服务端确认的真实管理员身份。 */
	public void checkAdministrator() {
		if (!isAdministrator()) {
			throw new BizException(ResultEnum.PERMISSION_ERROR, "仅超级管理员可使用此功能");
		}
	}

	public boolean isLogin() {
		return StpUtil.isLogin();
	}

	public String getToken() {
		return StpUtil.getTokenValue();
	}
}
