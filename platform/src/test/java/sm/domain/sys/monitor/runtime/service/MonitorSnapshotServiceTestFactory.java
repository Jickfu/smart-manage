package sm.domain.sys.monitor.runtime.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import tools.jackson.databind.json.JsonMapper;

/** 仅供跨包测试组装真实 SnapshotService，不扩大生产 Store 的可见性。 */
public final class MonitorSnapshotServiceTestFactory {
  private MonitorSnapshotServiceTestFactory() {}

  public static MonitorSnapshotService create(
      MonitorInstanceRegistry registry,
      StringRedisTemplate redisTemplate,
      JsonMapper jsonMapper,
      JdbcTemplate jdbcTemplate,
      MonitorProperties properties) {
    return new MonitorSnapshotService(
        registry,
        redisTemplate,
        jsonMapper,
        new MonitorSnapshotStore(properties),
        jdbcTemplate,
        properties);
  }
}
