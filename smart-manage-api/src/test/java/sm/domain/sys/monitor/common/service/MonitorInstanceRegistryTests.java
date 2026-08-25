package sm.domain.sys.monitor.common.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import sm.domain.sys.monitor.common.config.MonitorClusterProperties;
import sm.system.exception.BizException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonitorInstanceRegistryTests {

    @Test
    void redisUnavailableAbortsApplicationReadyAndExposesRuntimeHeartbeatFailure() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("offline"));
        MonitorClusterProperties properties = new MonitorClusterProperties();
        properties.setInternalBaseUrl("http://127.0.0.1:8080");
        MonitorInstanceRegistry registry = new MonitorInstanceRegistry(
                redisTemplate, JsonMapper.builder().build(), properties, mock(MonitorCatalogAccessor.class));
        ReflectionTestUtils.setField(registry, "instanceId", "instance1");
        ReflectionTestUtils.setField(registry, "applicationName", "smart-manage");
        ReflectionTestUtils.setField(registry, "applicationVersion", "test");
        ReflectionTestUtils.setField(registry, "configuredHostId", "test-host");

        assertThrows(BizException.class, registry::registerWhenReady);
        assertThrows(BizException.class, registry::scheduledHeartbeat);
        assertThrows(BizException.class, registry::heartbeat);
    }
}
