package sm.domain.sys.base.openapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.common.constant.BaseRedisKey;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.Duration;

/** Redis 原子消费请求 nonce，保证多实例部署下的重放防护一致。 */
@Component
@RequiredArgsConstructor
public class OpenApiNonceService {
    private final StringRedisTemplate redisTemplate;

    public void consume(String keyId, String nonce) {
        Boolean accepted = redisTemplate.opsForValue().setIfAbsent(
                BaseRedisKey.OPENAPI_NONCE + keyId + ":" + nonce, "1", Duration.ofMinutes(10));
        if (!Boolean.TRUE.equals(accepted)) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "OpenAPI 请求认证失败");
        }
    }
}
