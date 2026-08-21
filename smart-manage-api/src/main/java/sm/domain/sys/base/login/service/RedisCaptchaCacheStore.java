package sm.domain.sys.base.login.service;

import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.common.AnyMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.login.constant.LoginProtectionParam;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.concurrent.TimeUnit;

/**
 * 将验证码库的挑战答案保存到项目共享 Redis。
 *
 * <p>读取即删除保证同一挑战最多校验一次；验证码库传入的固定 TTL 被系统内置参数覆盖，
 * 从而让多实例运行参数修改立即生效。</p>
 */
@Component
@RequiredArgsConstructor
class RedisCaptchaCacheStore implements CacheStore {
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysParamService sysParamService;
    private final LoginRedisAccessor loginRedisAccessor;

    @Override
    public AnyMap getCache(String key) {
        return cast(redisTemplate.opsForValue().get(key));
    }

    @Override
    public AnyMap getAndRemoveCache(String key) {
        return cast(loginRedisAccessor.getAndDelete(key));
    }

    @Override
    public boolean setCache(String key, AnyMap value, Long ignoredExpire, TimeUnit ignoredTimeUnit) {
        int expireSeconds = requiredPositiveInt(LoginProtectionParam.CAPTCHA_CHALLENGE_EXPIRE_SECONDS);
        redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public Long incr(String key, long delta, Long expire, TimeUnit timeUnit) {
        Long value = redisTemplate.opsForValue().increment(key, delta);
        if (value != null && value == delta) {
            redisTemplate.expire(key, expire, timeUnit);
        }
        return value;
    }

    @Override
    public Long getLong(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    @Override
    public void close() {
        // RedisTemplate 生命周期由 Spring 管理。
    }

    private AnyMap cast(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof AnyMap anyMap) {
            return anyMap;
        }
        throw new BizException(ResultEnum.SERVER_ERROR, "认证服务暂不可用");
    }

    private int requiredPositiveInt(String number) {
        Integer value = sysParamService.getInt(number);
        if (value == null || value <= 0) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "登录保护参数 " + number + " 必须为正整数");
        }
        return value;
    }
}
