package sm.domain.sys.monitor.runtime.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.runtime.model.vo.RuntimeSnapshotVO;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/** 后台采样不依赖浏览器；实时态进入 Redis，固定一分钟桶进入 PostgreSQL。 */
@Component
@RequiredArgsConstructor
@Slf4j
class MonitorSnapshotSampler {
    private final RuntimeMonitorService runtimeMonitorService;
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final JdbcTemplate jdbcTemplate;

    @Value("${smart-manage.monitor.snapshot-ttl-seconds:40}")
    private long snapshotTtlSeconds;

    @Value("${smart-manage.monitor.history.retention-days:7}")
    private int retentionDays;

    @Scheduled(fixedDelayString = "${smart-manage.monitor.sample-interval-ms:10000}")
    void sampleCurrent() {
        try {
            RuntimeSnapshotVO snapshot = runtimeMonitorService.localSnapshot();
            String json = jsonMapper.writeValueAsString(snapshot);
            Duration ttl = Duration.ofSeconds(snapshotTtlSeconds);
            redisTemplate.opsForValue().set("sm:monitor:snapshot:instance:" + snapshot.getInstanceId(), json, ttl);
            redisTemplate.opsForValue().set("sm:monitor:snapshot:host:" + snapshot.getHostId(), json, ttl);
        } catch (Exception exception) {
            log.warn("监控实时采样失败", exception);
        }
    }

    @Scheduled(fixedDelayString = "${smart-manage.monitor.history-interval-ms:60000}")
    void persistHistory() {
        try {
            RuntimeSnapshotVO snapshot = runtimeMonitorService.localSnapshot();
            Instant sampleTime = Instant.now();
            Instant bucket = sampleTime.truncatedTo(ChronoUnit.MINUTES);
            OffsetDateTime jdbcSampleTime = OffsetDateTime.ofInstant(sampleTime, ZoneOffset.UTC);
            OffsetDateTime jdbcBucket = OffsetDateTime.ofInstant(bucket, ZoneOffset.UTC);
            long filesystemTotal = snapshot.getFilesystems().stream().mapToLong(RuntimeSnapshotVO.FilesystemInfo::getTotal).sum();
            long filesystemUsed = snapshot.getFilesystems().stream().mapToLong(RuntimeSnapshotVO.FilesystemInfo::getUsed).sum();
            jdbcTemplate.update("""
                    INSERT INTO t_sys_monitor_host_history(id,host_id,sample_bucket,sample_time,cpu_usage,load_average,
                    memory_total,memory_used,swap_total,swap_used,filesystem_total,filesystem_used,disk_read_rate,
                    disk_write_rate,network_receive_rate,network_transmit_rate) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    ON CONFLICT(host_id,sample_bucket) DO UPDATE SET sample_time=EXCLUDED.sample_time,cpu_usage=EXCLUDED.cpu_usage,
                    load_average=EXCLUDED.load_average,memory_total=EXCLUDED.memory_total,memory_used=EXCLUDED.memory_used,
                    swap_total=EXCLUDED.swap_total,swap_used=EXCLUDED.swap_used,filesystem_total=EXCLUDED.filesystem_total,
                    filesystem_used=EXCLUDED.filesystem_used,disk_read_rate=EXCLUDED.disk_read_rate,
                    disk_write_rate=EXCLUDED.disk_write_rate,network_receive_rate=EXCLUDED.network_receive_rate,
                    network_transmit_rate=EXCLUDED.network_transmit_rate
                    WHERE EXCLUDED.sample_time>=t_sys_monitor_host_history.sample_time
                    """, IdWorker.getId(), snapshot.getHostId(), jdbcBucket, jdbcSampleTime, snapshot.getCpu().getSystemUsage(),
                    snapshot.getCpu().getLoadAverage(), snapshot.getMemory().getPhysicalTotal(),
                    snapshot.getMemory().getPhysicalTotal() - snapshot.getMemory().getPhysicalAvailable(),
                    snapshot.getMemory().getSwapTotal(), snapshot.getMemory().getSwapUsed(), filesystemTotal, filesystemUsed,
                    snapshot.getIo().getDiskReadBytesPerSecond(), snapshot.getIo().getDiskWriteBytesPerSecond(),
                    snapshot.getIo().getNetworkReceiveBytesPerSecond(), snapshot.getIo().getNetworkTransmitBytesPerSecond());
            long gcCount = snapshot.getGc().stream().mapToLong(RuntimeSnapshotVO.GcInfo::getCollectionCount).filter(value -> value > 0).sum();
            long gcDuration = snapshot.getGc().stream().mapToLong(RuntimeSnapshotVO.GcInfo::getCollectionTimeMs).filter(value -> value > 0).sum();
            jdbcTemplate.update("""
                    INSERT INTO t_sys_monitor_instance_history(id,instance_id,host_id,sample_bucket,sample_time,process_cpu,
                    heap_used,heap_max,thread_count,blocked_thread_count,gc_count,gc_duration_ms,http_request_rate,
                    http_4xx_rate,http_5xx_rate,http_p95_ms,http_p99_ms,db_active,db_max,db_waiting,
                    health_status,database_status,redis_status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    ON CONFLICT(instance_id,sample_bucket) DO UPDATE SET sample_time=EXCLUDED.sample_time,
                    process_cpu=EXCLUDED.process_cpu,heap_used=EXCLUDED.heap_used,heap_max=EXCLUDED.heap_max,
                    thread_count=EXCLUDED.thread_count,blocked_thread_count=EXCLUDED.blocked_thread_count,
                    gc_count=EXCLUDED.gc_count,gc_duration_ms=EXCLUDED.gc_duration_ms,http_request_rate=EXCLUDED.http_request_rate,
                    http_4xx_rate=EXCLUDED.http_4xx_rate,http_5xx_rate=EXCLUDED.http_5xx_rate,http_p95_ms=EXCLUDED.http_p95_ms,
                    http_p99_ms=EXCLUDED.http_p99_ms,db_active=EXCLUDED.db_active,
                    db_max=EXCLUDED.db_max,db_waiting=EXCLUDED.db_waiting,health_status=EXCLUDED.health_status,
                    database_status=EXCLUDED.database_status,redis_status=EXCLUDED.redis_status
                    """, IdWorker.getId(), snapshot.getInstanceId(), snapshot.getHostId(), jdbcBucket, jdbcSampleTime,
                    snapshot.getCpu().getProcessUsage(), snapshot.getMemory().getHeapUsed(), snapshot.getMemory().getHeapMax(),
                    snapshot.getThreads().getLive(), snapshot.getThreads().getStateCounts().getOrDefault("BLOCKED", 0),
                    gcCount, gcDuration, snapshot.getHttp().getRequestRate(), snapshot.getHttp().getClientErrorRate(),
                    snapshot.getHttp().getServerErrorRate(), snapshot.getHttp().getP95Ms(), snapshot.getHttp().getP99Ms(),
                    snapshot.getDataSource().getActive(), snapshot.getDataSource().getMaxActive(),
                    snapshot.getDataSource().getWaiting(), snapshot.getHealth().getStatus(), component(snapshot, "db"),
                    component(snapshot, "redis"));
        } catch (Exception exception) {
            log.warn("监控历史持久化失败", exception);
        }
    }

    @Scheduled(cron = "${smart-manage.monitor.history-cleanup-cron:0 20 3 * * *}")
    void cleanupHistory() {
        OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(retentionDays);
        jdbcTemplate.update("DELETE FROM t_sys_monitor_host_history WHERE sample_time < ?", cutoff);
        jdbcTemplate.update("DELETE FROM t_sys_monitor_instance_history WHERE sample_time < ?", cutoff);
    }

    private String component(RuntimeSnapshotVO snapshot, String name) {
        return snapshot.getHealth().getComponents().stream().filter(item -> item.getName().equalsIgnoreCase(name))
                .map(RuntimeSnapshotVO.HealthComponent::getStatus).findFirst().orElse("UNKNOWN");
    }
}
