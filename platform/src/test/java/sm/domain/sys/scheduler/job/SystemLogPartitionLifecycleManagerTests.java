package sm.domain.sys.scheduler.job;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemLogPartitionLifecycleManagerTests {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-10T00:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void archiveMovesOnlyCompleteExpiredMonth() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), any()))
                .thenReturn(List.of("t_sys_login_log_p202601", "t_sys_login_log_p202602"))
                .thenReturn(List.of(), List.of(), List.of(), List.of());
        SystemLogPartitionLifecycleManager manager =
                new SystemLogPartitionLifecycleManager(jdbcTemplate, FIXED_CLOCK);

        int archived = manager.archive(settings(180));

        assertEquals(1, archived);
        verify(jdbcTemplate).execute(
                "ALTER TABLE public.t_sys_login_log DETACH PARTITION public.t_sys_login_log_p202601");
        verify(jdbcTemplate).execute("ALTER TABLE public.t_sys_login_log_history ATTACH PARTITION "
                + "public.t_sys_login_log_p202601 FOR VALUES FROM ('2026-01-01') TO ('2026-02-01')");
    }

    @Test
    void archiveKeepsJobPartitionWithUnknownExecution() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), any()))
                .thenReturn(List.of(), List.of(), List.of(), List.of(),
                        List.of("t_sys_job_log_p202601"));
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class))).thenReturn(true);
        SystemLogPartitionLifecycleManager manager =
                new SystemLogPartitionLifecycleManager(jdbcTemplate, FIXED_CLOCK);

        int archived = manager.archive(settings(90));

        assertEquals(0, archived);
    }

    @Test
    void purgeRecognizesArchivedPartitionWithOnlineTablePrefix() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), any()))
                .thenReturn(List.of("t_sys_login_log_p202501"))
                .thenReturn(List.of(), List.of(), List.of(), List.of());
        SystemLogPartitionLifecycleManager manager =
                new SystemLogPartitionLifecycleManager(jdbcTemplate, FIXED_CLOCK);

        int purged = manager.purge(settings(365));

        assertEquals(1, purged);
        verify(jdbcTemplate).execute("DROP TABLE public.t_sys_login_log_p202501");
    }

    private static SystemLogLifecycleSettings settings(int retentionDays) {
        EnumMap<SystemLogPartitionDefinition, Integer> values =
                new EnumMap<>(SystemLogPartitionDefinition.class);
        SystemLogPartitionDefinition.VALUES.forEach(definition -> values.put(definition, retentionDays));
        return new SystemLogLifecycleSettings(values, 12);
    }
}
