package sm.domain.sys.monitor.common.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** 将 Redis 在线态同步为可追溯目录；内部地址刻意不持久化。 */
@Component
@RequiredArgsConstructor
class MonitorCatalogAccessor {
    private final JdbcTemplate jdbcTemplate;

    void touch(MonitorInstanceRegistry.RegisteredInstance instance) {
        OffsetDateTime seenTime = OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(instance.getLastSeenTime()), ZoneOffset.UTC);
        jdbcTemplate.update("""
                INSERT INTO t_sys_monitor_host(id,host_id,host_name,os_name,os_version,arch,first_seen_time,last_seen_time)
                VALUES(?,?,?,?,?,?,?,?)
                ON CONFLICT(host_id) DO UPDATE SET host_name=EXCLUDED.host_name,os_name=EXCLUDED.os_name,
                os_version=EXCLUDED.os_version,arch=EXCLUDED.arch,last_seen_time=EXCLUDED.last_seen_time
                """, IdWorker.getId(), instance.getHostId(), instance.getHostName(), instance.getOsName(),
                instance.getOsVersion(), instance.getArch(), seenTime, seenTime);
        jdbcTemplate.update("""
                INSERT INTO t_sys_monitor_instance(id,instance_id,host_id,application_name,application_version,
                first_seen_time,last_seen_time,last_start_time) VALUES(?,?,?,?,?,?,?,?)
                ON CONFLICT(instance_id) DO UPDATE SET host_id=EXCLUDED.host_id,application_name=EXCLUDED.application_name,
                application_version=EXCLUDED.application_version,last_seen_time=EXCLUDED.last_seen_time,
                last_start_time=EXCLUDED.last_start_time
                """, IdWorker.getId(), instance.getInstanceId(), instance.getHostId(), instance.getApplicationName(),
                instance.getApplicationVersion(), seenTime, seenTime,
                OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(instance.getStartTime()), ZoneOffset.UTC));
    }
}
