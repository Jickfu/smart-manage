package sm.domain.sys.base.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/** 登录领域需要的 Redis 原子操作，使用 Lua 兼容不支持 GETDEL 的 Redis 版本。 */
@Component
@RequiredArgsConstructor
class LoginRedisAccessor {
    private static final DefaultRedisScript<String> GET_AND_DELETE_SCRIPT = new DefaultRedisScript<>(
            "local value = redis.call('GET', KEYS[1]); "
                    + "if value then redis.call('DEL', KEYS[1]); end; "
                    + "return value;",
            String.class);

    private final StringRedisTemplate redisTemplate;

    public String getAndDelete(String key) {
        return redisTemplate.execute(GET_AND_DELETE_SCRIPT, List.of(key));
    }
}
