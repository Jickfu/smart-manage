package sm.domain.sys.monitor.runtime.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.common.config.MonitorProperties;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;
import tools.jackson.databind.json.JsonMapper;

/** 唯一采样源：每周期只采集一次，再原子发布给 Redis、历史和告警消费者。 */
@Component
@RequiredArgsConstructor
@Slf4j
class MonitorSnapshotSampler {
  private static final DefaultRedisScript<Long> PUBLISH_SCRIPT =
      new DefaultRedisScript<>(
          """
          redis.call('PSETEX', KEYS[1], ARGV[1], ARGV[2])
          redis.call('PSETEX', KEYS[2], ARGV[1], ARGV[3])
          return 1
          """,
          Long.class);

  private final OshiHostMetricsProvider hostProvider;
  private final ApplicationMetricsProvider applicationProvider;
  private final MonitorSnapshotStore store;
  private final StringRedisTemplate redisTemplate;
  private final JsonMapper jsonMapper;
  private final JdbcTemplate jdbcTemplate;
  private final MonitorProperties properties;

  @Scheduled(fixedDelayString = "${smart-manage.monitor.sampling.interval-ms}")
  void sampleCurrent() {
    Instant sampleTime = Instant.now();
    try {
      HostSnapshotVO host = hostProvider.collect(sampleTime);
      InstanceSnapshotVO instance = applicationProvider.collect(sampleTime);
      store.publish(host, instance);
      Duration ttl = Duration.ofSeconds(properties.getSampling().getSnapshotTtlSeconds());
      redisTemplate.execute(
          PUBLISH_SCRIPT,
          java.util.List.of(
              "sm:monitor:snapshot:host:" + host.getHostId(),
              "sm:monitor:snapshot:instance:" + instance.getInstanceId()),
          Long.toString(ttl.toMillis()),
          jsonMapper.writeValueAsString(host),
          jsonMapper.writeValueAsString(instance));
    } catch (Exception exception) {
      log.warn("监控实时采样失败", exception);
    }
  }

  @Scheduled(fixedDelayString = "${smart-manage.monitor.history.interval-ms}")
  void persistHistory() {
    MonitorSnapshotStore.SnapshotPair pair = store.current();
    if (pair == null) return;
    try {
      persistHost(pair.host());
      persistInstance(pair.instance());
    } catch (Exception exception) {
      log.warn("监控历史持久化失败", exception);
    }
  }

  private void persistHost(HostSnapshotVO snapshot) {
    OffsetDateTime sample = OffsetDateTime.ofInstant(snapshot.getSampleTime(), ZoneOffset.UTC);
    OffsetDateTime bucket = sample.truncatedTo(ChronoUnit.MINUTES);
    HostSnapshotVO.FilesystemInfo worst =
        snapshot.getFilesystems().stream()
            .max(Comparator.comparing(item -> item.getUsage() == null ? 0d : item.getUsage()))
            .orElse(null);
    long total =
        snapshot.getFilesystems().stream().mapToLong(HostSnapshotVO.FilesystemInfo::getTotal).sum();
    long used =
        snapshot.getFilesystems().stream().mapToLong(HostSnapshotVO.FilesystemInfo::getUsed).sum();
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_host_history(id,host_id,sample_bucket,sample_time,cpu_usage,load_average,memory_total,memory_used,
swap_total,swap_used,filesystem_total,filesystem_used,disk_read_rate,disk_write_rate,network_receive_rate,network_transmit_rate,
worst_filesystem_usage,worst_mount) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
ON CONFLICT(host_id,sample_bucket) DO UPDATE SET sample_time=EXCLUDED.sample_time,cpu_usage=EXCLUDED.cpu_usage,
load_average=EXCLUDED.load_average,memory_total=EXCLUDED.memory_total,memory_used=EXCLUDED.memory_used,swap_total=EXCLUDED.swap_total,
swap_used=EXCLUDED.swap_used,filesystem_total=EXCLUDED.filesystem_total,filesystem_used=EXCLUDED.filesystem_used,
disk_read_rate=EXCLUDED.disk_read_rate,disk_write_rate=EXCLUDED.disk_write_rate,network_receive_rate=EXCLUDED.network_receive_rate,
network_transmit_rate=EXCLUDED.network_transmit_rate,worst_filesystem_usage=EXCLUDED.worst_filesystem_usage,worst_mount=EXCLUDED.worst_mount
WHERE EXCLUDED.sample_time>=t_sys_monitor_host_history.sample_time
""",
        IdWorker.getId(),
        snapshot.getHostId(),
        bucket,
        sample,
        snapshot.getCpu().getUsage(),
        snapshot.getCpu().getLoadAverage(),
        snapshot.getMemory().getTotal(),
        snapshot.getMemory().getTotal() - snapshot.getMemory().getAvailable(),
        snapshot.getMemory().getSwapTotal(),
        snapshot.getMemory().getSwapUsed(),
        total,
        used,
        snapshot.getIo().getDiskReadBytesPerSecond(),
        snapshot.getIo().getDiskWriteBytesPerSecond(),
        snapshot.getIo().getNetworkReceiveBytesPerSecond(),
        snapshot.getIo().getNetworkTransmitBytesPerSecond(),
        worst == null ? null : worst.getUsage(),
        worst == null ? null : worst.getMount());
  }

  private void persistInstance(InstanceSnapshotVO snapshot) {
    OffsetDateTime sample = OffsetDateTime.ofInstant(snapshot.getSampleTime(), ZoneOffset.UTC);
    OffsetDateTime bucket = sample.truncatedTo(ChronoUnit.MINUTES);
    long gcCount =
        snapshot.getGc().stream()
            .mapToLong(InstanceSnapshotVO.GcInfo::getCollectionCount)
            .filter(value -> value > 0)
            .sum();
    long gcDuration =
        snapshot.getGc().stream()
            .mapToLong(InstanceSnapshotVO.GcInfo::getCollectionTimeMs)
            .filter(value -> value > 0)
            .sum();
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_instance_history(id,instance_id,host_id,sample_bucket,sample_time,process_cpu,heap_used,heap_max,
thread_count,blocked_thread_count,gc_count,gc_duration_ms,http_request_rate,http_4xx_rate,http_5xx_rate,http_p95_ms,http_p99_ms,
db_active,db_max,db_waiting,health_status,database_status,redis_status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
ON CONFLICT(instance_id,sample_bucket) DO UPDATE SET sample_time=EXCLUDED.sample_time,process_cpu=EXCLUDED.process_cpu,
heap_used=EXCLUDED.heap_used,heap_max=EXCLUDED.heap_max,thread_count=EXCLUDED.thread_count,blocked_thread_count=EXCLUDED.blocked_thread_count,
gc_count=EXCLUDED.gc_count,gc_duration_ms=EXCLUDED.gc_duration_ms,http_request_rate=EXCLUDED.http_request_rate,
http_4xx_rate=EXCLUDED.http_4xx_rate,http_5xx_rate=EXCLUDED.http_5xx_rate,http_p95_ms=EXCLUDED.http_p95_ms,http_p99_ms=EXCLUDED.http_p99_ms,
db_active=EXCLUDED.db_active,db_max=EXCLUDED.db_max,db_waiting=EXCLUDED.db_waiting,health_status=EXCLUDED.health_status,
database_status=EXCLUDED.database_status,redis_status=EXCLUDED.redis_status
WHERE EXCLUDED.sample_time>=t_sys_monitor_instance_history.sample_time
""",
        IdWorker.getId(),
        snapshot.getInstanceId(),
        snapshot.getHostId(),
        bucket,
        sample,
        snapshot.getCpu().getProcessUsage(),
        snapshot.getMemory().getHeapUsed(),
        snapshot.getMemory().getHeapMax(),
        snapshot.getThreads().getLive(),
        snapshot.getThreads().getStateCounts().getOrDefault("BLOCKED", 0),
        gcCount,
        gcDuration,
        snapshot.getHttp().getRequestRate(),
        snapshot.getHttp().getClientErrorRate(),
        snapshot.getHttp().getServerErrorRate(),
        snapshot.getHttp().getP95Ms(),
        snapshot.getHttp().getP99Ms(),
        snapshot.getDataSource().getActive(),
        snapshot.getDataSource().getMaxActive(),
        snapshot.getDataSource().getWaiting(),
        snapshot.getHealth().getStatus(),
        component(snapshot, "db"),
        component(snapshot, "redis"));
  }

  @Scheduled(cron = "${smart-manage.monitor.history.cleanup-cron}")
  void cleanupHistory() {
    OffsetDateTime cutoff =
        OffsetDateTime.now(ZoneOffset.UTC).minusDays(properties.getHistory().getRetentionDays());
    jdbcTemplate.update("DELETE FROM t_sys_monitor_host_history WHERE sample_time < ?", cutoff);
    jdbcTemplate.update("DELETE FROM t_sys_monitor_instance_history WHERE sample_time < ?", cutoff);
  }

  private String component(InstanceSnapshotVO snapshot, String name) {
    return snapshot.getHealth().getComponents().stream()
        .filter(item -> item.getName().equalsIgnoreCase(name))
        .map(InstanceSnapshotVO.HealthComponent::getStatus)
        .findFirst()
        .orElse("UNKNOWN");
  }
}
