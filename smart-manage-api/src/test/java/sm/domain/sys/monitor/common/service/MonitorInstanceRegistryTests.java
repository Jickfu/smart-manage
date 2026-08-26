package sm.domain.sys.monitor.common.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.system.exception.BizException;
import tools.jackson.databind.json.JsonMapper;

class MonitorInstanceRegistryTests {

  @Test
  void redisUnavailableAbortsApplicationReadyAndExposesRuntimeHeartbeatFailure() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("offline"));
    MonitorProperties properties = new MonitorProperties();
    properties.getCluster().setInternalBaseUrl("http://127.0.0.1:8080");
    MonitorInstanceRegistry registry =
        new MonitorInstanceRegistry(
            redisTemplate,
            JsonMapper.builder().build(),
            properties,
            mock(MonitorCatalogAccessor.class));
    ReflectionTestUtils.setField(registry, "instanceId", "instance1");
    ReflectionTestUtils.setField(registry, "applicationName", "smart-manage");
    ReflectionTestUtils.setField(registry, "applicationVersion", "test");
    properties.setHostId("test-host");

    assertThrows(BizException.class, registry::registerWhenReady);
    assertThrows(BizException.class, registry::scheduledHeartbeat);
    assertThrows(BizException.class, registry::heartbeat);
  }

  @Test
  @SuppressWarnings("unchecked")
  void duplicateActiveInstanceIdFailsFast() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    when(redisTemplate.execute(
            any(org.springframework.data.redis.core.script.RedisScript.class),
            anyList(),
            any(Object[].class)))
        .thenReturn(0L);
    MonitorProperties properties = new MonitorProperties();
    properties.setHostId("test-host");
    properties.getCluster().setInternalBaseUrl("http://127.0.0.1:8080");
    MonitorInstanceRegistry registry =
        new MonitorInstanceRegistry(
            redisTemplate,
            JsonMapper.builder().build(),
            properties,
            mock(MonitorCatalogAccessor.class));
    ReflectionTestUtils.setField(registry, "instanceId", "instance1");
    ReflectionTestUtils.setField(registry, "applicationName", "smart-manage");
    ReflectionTestUtils.setField(registry, "applicationVersion", "test");
    assertThrows(BizException.class, registry::registerWhenReady);

    registry.unregister();

    // 重复 ID 进程并不拥有注册租约，退出时不能误删真实进程的在线索引。
    verify(redisTemplate, never()).opsForZSet();
  }

  @Test
  @SuppressWarnings("unchecked")
  void scheduledHeartbeatUsesRedisWithoutDatabaseLifecyclePolling() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ZSetOperations<String, String> zSet = mock(ZSetOperations.class);
    when(redisTemplate.execute(
            any(org.springframework.data.redis.core.script.RedisScript.class),
            anyList(),
            any(Object[].class)))
        .thenReturn(1L);
    when(redisTemplate.opsForZSet()).thenReturn(zSet);
    MonitorCatalogAccessor catalog = mock(MonitorCatalogAccessor.class);
    MonitorProperties properties = new MonitorProperties();
    properties.setHostId("test-host");
    properties.getCluster().setInternalBaseUrl("http://127.0.0.1:8080");
    MonitorInstanceRegistry registry =
        new MonitorInstanceRegistry(
            redisTemplate, JsonMapper.builder().build(), properties, catalog);
    ReflectionTestUtils.setField(registry, "instanceId", "instance1");
    ReflectionTestUtils.setField(registry, "applicationName", "smart-manage");
    ReflectionTestUtils.setField(registry, "applicationVersion", "test");

    registry.registerWhenReady();
    clearInvocations(catalog);
    registry.scheduledHeartbeat();

    verify(catalog, never()).reactivateIfRetired(anyString());
    verify(catalog, never()).touch(any());
  }
}
