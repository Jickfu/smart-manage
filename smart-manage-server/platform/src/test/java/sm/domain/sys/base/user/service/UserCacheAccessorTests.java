package sm.domain.sys.base.user.service;

import com.alicp.jetcache.support.JavaValueEncoder;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.UserCacheSnapshot;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCacheAccessorTests {
    @Test
    void cacheSnapshotContainsOnlyRequiredNonAuthenticationFields() {
        Set<String> fieldNames = Arrays.stream(UserCacheSnapshot.class.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .map(field -> field.getName())
                .collect(Collectors.toSet());

        assertEquals(Set.of("id", "username", "name", "avatarAttachmentId"), fieldNames);
    }

    @Test
    void actualRemoteCacheEncodingDoesNotContainPasswordHash() {
        String passwordHash = "$argon2id$v=19$m=65536,t=3,p=1$test-salt$test-password-hash";
        UserCacheSnapshot snapshot = new UserCacheSnapshot(1L, "administrator", "管理员", 9L);

        byte[] encoded = JavaValueEncoder.INSTANCE.apply(snapshot);
        String binaryText = new String(encoded, StandardCharsets.ISO_8859_1);

        assertFalse(binaryText.contains(passwordHash));
        assertFalse(binaryText.toLowerCase().contains("password"));
    }

    @Test
    void accessorLoadsDedicatedProjectionInsteadOfUserEntity() {
        UserMapper mapper = mock(UserMapper.class);
        UserCacheSnapshot expected = new UserCacheSnapshot(1L, "user", "用户", 9L);
        when(mapper.selectCacheSnapshotById(1L)).thenReturn(expected);

        UserCacheSnapshot actual = new UserCacheAccessor(mapper).requireUser(1L);

        assertSame(expected, actual);
        verify(mapper).selectCacheSnapshotById(1L);
    }
}
