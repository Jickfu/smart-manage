package sm.domain.sys.base.user.apppin.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.app.model.vo.AppVO;
import sm.domain.sys.base.app.service.AppService;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.user.apppin.mapper.UserAppPinMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAppPinServiceTests {
	@Test
	void pinResolvesAccessibleApplicationBeforeWritingUserPreference() {
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		AppService appService = mock(AppService.class);
		UserAppPinTxService txService = mock(UserAppPinTxService.class);
		when(currentUserContext.getUserId()).thenReturn(10L);
		AppVO app = new AppVO();
		app.setId(20L);
		when(appService.getUserAppByNumber(10L, "base")).thenReturn(app);
		UserAppPinService service = new UserAppPinService(currentUserContext, appService,
				mock(UserAppPinMapper.class), txService);

		service.pin(" base ");

		verify(appService).getUserAppByNumber(10L, "base");
		verify(txService).pin(10L, 20L);
	}

	@Test
	void listUsesCurrentOrganizationButKeepsPreferenceUserScoped() {
		CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
		UserAppPinMapper mapper = mock(UserAppPinMapper.class);
		when(currentUserContext.getUserId()).thenReturn(10L);
		when(currentUserContext.getOrgId()).thenReturn(30L);
		when(currentUserContext.isAdministrator()).thenReturn(false);
		UserAppPinService service = new UserAppPinService(currentUserContext, mock(AppService.class),
				mapper, mock(UserAppPinTxService.class));

		service.listCurrentUserPins();

		verify(mapper).selectUserPins(10L, 30L, false);
	}
}
