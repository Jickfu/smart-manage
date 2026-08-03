package sm.domain.sys.base.common.helper;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.config.OrgConfig;

/** 当前登录用户的 Sa-Token 会话上下文。 */
@Component
@RequiredArgsConstructor
public class CurrentUserContext {
	private final OrgConfig orgConfig;

	public Long getUserId() {
		return StpUtil.getLoginIdAsLong();
	}

	public Long getOrgId() {
		Long orgId = StpUtil.getTokenSession().getLong("orgId");
		return orgId != null ? orgId : orgConfig.getDefaultId();
	}

	public void setOrgId(Long orgId) {
		StpUtil.getTokenSession().set("orgId", orgId);
	}

	public boolean isLogin() {
		return StpUtil.isLogin();
	}

	public String getToken() {
		return StpUtil.getTokenValue();
	}
}
