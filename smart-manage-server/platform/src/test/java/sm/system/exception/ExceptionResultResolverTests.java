package sm.system.exception;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;
import sm.system.response.ResultEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionResultResolverTests {

    @Test
    void queryTimeoutHasSafeDistinctFeedbackWithoutImplyingWriteRollback() {
        var result = ExceptionResultResolver.resolve(
                new org.springframework.dao.QueryTimeoutException("private query text"));
        assertEquals(ResultEnum.QUERY_TIMEOUT.getCode(), result.getCode());
        assertEquals(ResultEnum.QUERY_TIMEOUT.getMsg(), result.getMsg());
        assertEquals(sm.system.response.FeedbackLevel.ERROR, result.getFeedbackLevel());
    }

    @Test
    void redisConnectionFailureUsesConfigurationErrorInsteadOfGenericPersistenceError() {
        var result = ExceptionResultResolver.resolve(new RedisConnectionFailureException("offline"));

        assertEquals(ResultEnum.CONFIG_ERROR.getCode(), result.getCode());
    }

    @Test
    void redisSerializationFailureUsesConfigurationErrorInsteadOfGenericPersistenceError() {
        var result = ExceptionResultResolver.resolve(new SerializationException("invalid payload"));

        assertEquals(ResultEnum.CONFIG_ERROR.getCode(), result.getCode());
    }
}
