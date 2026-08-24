package sm.domain.sys.base.user.apppin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.app.model.vo.AppVO;
import sm.domain.sys.base.user.apppin.mapper.UserAppPinMapper;
import sm.domain.sys.base.user.apppin.model.vo.PinnedAppVO;
import sm.domain.sys.base.app.service.AppService;
import sm.system.security.context.CurrentUserContext;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAppPinService {
	private final CurrentUserContext currentUserContext;
	private final AppService appService;
	private final UserAppPinMapper mapper;
	private final UserAppPinTxService txService;

	public List<PinnedAppVO> listCurrentUserPins() {
		return mapper.selectUserPins(currentUserContext.getUserId(), currentUserContext.getOrgId(),
				currentUserContext.isAdministrator());
	}

	public void pin(String appNumber) {
		Long userId = currentUserContext.getUserId();
		AppVO app = appService.getUserAppByNumber(userId, appNumber.trim());
		txService.pin(userId, app.getId());
	}

	public void unpin(String appNumber) {
		txService.unpin(currentUserContext.getUserId(), appNumber.trim());
	}
}
