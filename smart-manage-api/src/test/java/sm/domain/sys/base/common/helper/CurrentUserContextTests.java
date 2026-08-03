package sm.domain.sys.base.common.helper;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sm.domain.sys.base.common.config.OrgConfig;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrentUserContextTests {

	@Test
	void initializeIdentityStoresServerVerifiedClaims() {
		OrgConfig orgConfig = new OrgConfig();
		orgConfig.setDefaultId(1L);
		SaSession session = mock(SaSession.class);
		CurrentUserContext context = new CurrentUserContext(orgConfig);
		try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
			stpUtil.when(StpUtil::getTokenSession).thenReturn(session);
			context.initializeIdentity("administrator", true);
		}

		verify(session).set("orgId", 1L);
		verify(session).set("username", "administrator");
		verify(session).set("administrator", true);
	}

	@Test
	void missingAdministratorClaimFailsClosed() {
		SaSession session = mock(SaSession.class);
		CurrentUserContext context = new CurrentUserContext(new OrgConfig());
		try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
			stpUtil.when(StpUtil::isLogin).thenReturn(true);
			stpUtil.when(StpUtil::getTokenSession).thenReturn(session);

			assertFalse(context.isAdministrator());
			assertThrows(BizException.class, context::checkAdministrator);
		}
	}

	@Test
	void verifiedAdministratorClaimPassesCheck() {
		SaSession session = mock(SaSession.class);
		when(session.get("administrator")).thenReturn(true);
		CurrentUserContext context = new CurrentUserContext(new OrgConfig());
		try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
			stpUtil.when(StpUtil::isLogin).thenReturn(true);
			stpUtil.when(StpUtil::getTokenSession).thenReturn(session);

			assertTrue(context.isAdministrator());
		}
	}
}
