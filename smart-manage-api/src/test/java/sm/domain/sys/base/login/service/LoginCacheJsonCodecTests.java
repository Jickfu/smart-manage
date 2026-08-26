package sm.domain.sys.base.login.service;

import cloud.tianai.captcha.common.AnyMap;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.login.model.TemporaryLoginGrant;
import sm.infrastructure.json.JsonConfig;
import sm.system.exception.BizException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginCacheJsonCodecTests {
    private final LoginCacheJsonCodec codec = new LoginCacheJsonCodec(jsonMapper());

    @Test
    void temporaryLoginGrantRoundTripDoesNotWriteJavaTypeMetadata() {
        TemporaryLoginGrant grant = new TemporaryLoginGrant(
                "grant-id", 1L, 9L, "target", "排障", LocalDateTime.of(2026, 8, 26, 20, 0));

        String json = codec.write(grant);
        TemporaryLoginGrant restored = codec.read(json, TemporaryLoginGrant.class);

        assertFalse(json.contains("@class"));
        assertFalse(json.contains(TemporaryLoginGrant.class.getName()));
        assertEquals(grant, restored);
    }

    @Test
    void captchaChallengeRoundTripUsesExplicitAnyMapType() {
        AnyMap challenge = AnyMap.create().set("x", 12).set("token", "opaque");

        String json = codec.write(challenge);
        AnyMap restored = codec.read(json, AnyMap.class);

        assertEquals(12, restored.getInt("x"));
        assertEquals("opaque", restored.getString("token"));
    }

    @Test
    void javaTypeMetadataIsRejectedInsteadOfInstantiatingDeclaredClass() {
        String maliciousJson = """
                {"@class":"java.util.HashMap","grantId":"grant-id","issuerUserId":"1",\
                "targetUserId":"9","targetUsername":"target","reason":"排障",\
                "expiresAt":"2026-08-26 20:00:00"}
                """;

        assertThrows(BizException.class,
                () -> codec.read(maliciousJson, TemporaryLoginGrant.class));
    }

    private JsonMapper jsonMapper() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new JsonConfig().customizer().customize(builder);
        return builder.build();
    }
}
