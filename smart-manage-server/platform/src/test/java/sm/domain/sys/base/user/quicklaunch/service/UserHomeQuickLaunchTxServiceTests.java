package sm.domain.sys.base.user.quicklaunch.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sm.domain.sys.base.user.quicklaunch.mapper.UserHomeQuickLaunchMapper;
import sm.domain.sys.base.user.quicklaunch.model.entity.UserHomeQuickLaunchEntity;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;
import sm.system.exception.BizException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserHomeQuickLaunchTxServiceTests {
    private final UserHomeQuickLaunchMapper mapper = mock(UserHomeQuickLaunchMapper.class);
    private final UserHomeQuickLaunchTxService service = new UserHomeQuickLaunchTxService(mapper);

    @Test
    void replaceLocksUserAndWritesStableSequence() {
        when(mapper.insert(any(UserHomeQuickLaunchEntity.class))).thenReturn(1);
        ArgumentCaptor<UserHomeQuickLaunchEntity> entityCaptor =
                ArgumentCaptor.forClass(UserHomeQuickLaunchEntity.class);

        service.replace(10L, HomeScopeEnum.APPLICATION, 20L, List.of(101L, 100L));

        verify(mapper).lockUser(10L);
        verify(mapper).deleteScope(10L, HomeScopeEnum.APPLICATION, 20L);
        verify(mapper, org.mockito.Mockito.times(2)).insert(entityCaptor.capture());
        assertEquals(List.of(101L, 100L), entityCaptor.getAllValues().stream()
                .map(UserHomeQuickLaunchEntity::getMenuId).toList());
        assertEquals(List.of(1, 2), entityCaptor.getAllValues().stream()
                .map(UserHomeQuickLaunchEntity::getSeq).toList());
    }

    @Test
    void replaceFailsWhenAnyPreferenceCannotBePersisted() {
        when(mapper.insert(any(UserHomeQuickLaunchEntity.class))).thenReturn(0);

        assertThrows(BizException.class,
                () -> service.replace(10L, HomeScopeEnum.SYSTEM, null, List.of(100L)));
    }
}
