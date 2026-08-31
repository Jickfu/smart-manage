package sm.system.security.context;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.openapi.OpenApiActorContext;

/** 当前登录用户的 Sa-Token 会话上下文。 */
@Component
public class CurrentUserContext {
	private static final String ORG_ID_KEY = "orgId";
	private static final String USERNAME_KEY = "username";
	private static final String ADMINISTRATOR_KEY = "administrator";

	/** 凭据验证并建立正式登录态后，集中初始化服务端认证声明。 */
	public void initializeIdentity(Long orgId, String username, boolean administrator) {
		var session = StpUtil.getTokenSession();
		setIdentityClaims(session, orgId, username, administrator);
	}

	/** 为显式创建的独立登录令牌初始化服务端认证声明。 */
	public void initializeIdentity(String token, Long orgId, String username, boolean administrator) {
		var session = StpUtil.getStpLogic().getTokenSessionByToken(token);
		setIdentityClaims(session, orgId, username, administrator);
	}

	private void setIdentityClaims(cn.dev33.satoken.session.SaSession session, Long orgId,
			String username, boolean administrator) {
		session.set(ORG_ID_KEY, orgId);
		session.set(USERNAME_KEY, username);
		session.set(ADMINISTRATOR_KEY, administrator);
	}

	public Long getUserId() {
		OpenApiActorContext.Actor actor = OpenApiActorContext.currentOrNull();
		if (actor != null) {
			return actor.userId();
		}
		return StpUtil.getLoginIdAsLong();
	}

	public Long getOrgId() {
		OpenApiActorContext.Actor actor = OpenApiActorContext.currentOrNull();
		if (actor != null) {
			return actor.orgId();
		}
		Object orgIdClaim = StpUtil.getTokenSession().get(ORG_ID_KEY);
		if (!(orgIdClaim instanceof Number orgIdNumber) || orgIdNumber.longValue() <= 0) {
			throw new BizException(ResultEnum.UNAUTHORIZED, "当前登录会话缺少组织上下文，请重新登录");
		}
		return orgIdNumber.longValue();
	}

	public void setOrgId(Long orgId) {
		if (OpenApiActorContext.currentOrNull() != null) {
			throw new BizException(ResultEnum.PERMISSION_ERROR, "外部 API 代理身份不允许切换组织");
		}
		StpUtil.getTokenSession().set(ORG_ID_KEY, orgId);
	}

	public String getUsernameOrDefault(String defaultUsername) {
		OpenApiActorContext.Actor actor = OpenApiActorContext.currentOrNull();
		if (actor != null) {
			return actor.username();
		}
		String username = StpUtil.getTokenSession().getString(USERNAME_KEY);
		return username != null ? username : defaultUsername;
	}

	public boolean isAdministrator() {
		if (OpenApiActorContext.currentOrNull() != null) {
			return false;
		}
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
		return OpenApiActorContext.currentOrNull() != null || StpUtil.isLogin();
	}

	public String getToken() {
		if (OpenApiActorContext.currentOrNull() != null) {
			return null;
		}
		return StpUtil.getTokenValue();
	}
}
