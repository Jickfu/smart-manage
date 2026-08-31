package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.user.contract.UserReference;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserReferenceServiceTests {

    @Test
    void requireReturnsDisabledUserForHistoricalRead() {
        UserMapper mapper = mock(UserMapper.class);
        when(mapper.selectById(1L)).thenReturn(user(1L, false));

        UserReference reference = new UserReferenceService(mapper).require(1L);

        assertEquals(1L, reference.id());
        assertEquals("USER-1", reference.number());
        assertEquals("用户1", reference.name());
        assertFalse(reference.enabled());
    }

    @Test
    void requireRejectsMissingUser() {
        UserMapper mapper = mock(UserMapper.class);
        when(mapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
                () -> new UserReferenceService(mapper).require(1L));

        assertEquals(ResultEnum.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void requireEnabledRejectsDisabledUserWithProviderLanguage() {
        UserMapper mapper = mock(UserMapper.class);
        when(mapper.selectById(1L)).thenReturn(user(1L, false));

        BizException exception = assertThrows(BizException.class,
                () -> new UserReferenceService(mapper).requireEnabled(1L));

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMsg().contains("用户已禁用，不能作为业务引用: 1"));
    }

    @Test
    void findByIdsUsesOneBatchAndKeepsInputOrderWhileAllowingMissingUsers() {
        UserMapper mapper = mock(UserMapper.class);
        List<Long> userIds = List.of(2L, 1L, 3L, 2L);
        when(mapper.selectByIds(List.of(2L, 1L, 3L)))
                .thenReturn(List.of(user(1L, true), user(2L, false)));

        Map<Long, UserReference> references = new UserReferenceService(mapper).findByIds(userIds);

        assertEquals(List.of(2L, 1L), references.keySet().stream().toList());
        assertFalse(references.get(2L).enabled());
        verify(mapper).selectByIds(List.of(2L, 1L, 3L));
    }

    @Test
    void requireEnabledByIdsRejectsMissingUserAfterOneBatch() {
        UserMapper mapper = mock(UserMapper.class);
        when(mapper.selectByIds(List.of(1L, 2L))).thenReturn(List.of(user(1L, true)));

        BizException exception = assertThrows(BizException.class,
                () -> new UserReferenceService(mapper).requireEnabledByIds(List.of(1L, 2L)));

        assertEquals(ResultEnum.NOT_FOUND.getCode(), exception.getCode());
        verify(mapper).selectByIds(List.of(1L, 2L));
    }

    @Test
    void requireEnabledByIdsRejectsDisabledUserAfterOneBatch() {
        UserMapper mapper = mock(UserMapper.class);
        when(mapper.selectByIds(List.of(1L, 2L)))
                .thenReturn(List.of(user(2L, false), user(1L, true)));

        BizException exception = assertThrows(BizException.class,
                () -> new UserReferenceService(mapper).requireEnabledByIds(List.of(1L, 2L)));

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), exception.getCode());
        verify(mapper).selectByIds(List.of(1L, 2L));
    }

    @Test
    void requireEnabledByIdsReturnsAllReferencesInInputOrder() {
        UserMapper mapper = mock(UserMapper.class);
        when(mapper.selectByIds(List.of(2L, 1L)))
                .thenReturn(List.of(user(1L, true), user(2L, true)));

        Map<Long, UserReference> references =
                new UserReferenceService(mapper).requireEnabledByIds(List.of(2L, 1L, 2L));

        assertEquals(List.of(2L, 1L), references.keySet().stream().toList());
        verify(mapper).selectByIds(List.of(2L, 1L));
    }

    @Test
    void emptyBatchDoesNotQueryPersistence() {
        UserMapper mapper = mock(UserMapper.class);
        UserReferenceService service = new UserReferenceService(mapper);

        assertEquals(Map.of(), service.findByIds(List.of()));
        assertEquals(Map.of(), service.requireEnabledByIds(null));
        verifyNoInteractions(mapper);
    }

    private static UserEntity user(Long id, boolean enabled) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setNumber("USER-" + id);
        user.setName("用户" + id);
        user.setEnabled(enabled);
        return user;
    }
}
