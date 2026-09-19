package sm.domain.sys.base.user.apppin.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.user.apppin.mapper.UserAppPinMapper;
import sm.domain.sys.base.user.apppin.model.entity.UserAppPinEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAppPinTxServiceTests {
	private final UserAppPinMapper mapper = mock(UserAppPinMapper.class);
	private final UserAppPinTxService service = new UserAppPinTxService(mapper);

	@Test
	void duplicatePinIsIdempotentAfterLockingUserOrder() {
		when(mapper.selectCount(any())).thenReturn(1L);

		service.pin(10L, 20L);

		verify(mapper).lockUser(10L);
		verify(mapper, never()).insert(any(UserAppPinEntity.class));
	}

	@Test
	void newPinAppendsUsingNextUserSequence() {
		when(mapper.selectCount(any())).thenReturn(0L);
		when(mapper.selectNextSeq(10L)).thenReturn(3);
		when(mapper.insert(any(UserAppPinEntity.class))).thenReturn(1);

		service.pin(10L, 20L);

		verify(mapper).lockUser(10L);
		verify(mapper).selectNextSeq(10L);
		verify(mapper).insert(any(UserAppPinEntity.class));
	}

	@Test
	void unpinOnlyDeletesCurrentUsersRelation() {
		service.unpin(10L, "base");

		verify(mapper).deleteByUserAndAppNumber(10L, "base");
	}
}
