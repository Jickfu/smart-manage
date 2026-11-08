package sm.domain.sys.scheduler.home.service;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import sm.domain.sys.scheduler.mapper.*;
import sm.domain.sys.scheduler.model.vo.SchedulerDailyCountVO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SchedulerHomeServiceTests {
    @Test
    void summaryReadsClockOnceAndDoesNotFetchDetailsOrRepeatQueries() {
        JobMapper jobs = mock(JobMapper.class);
        JobLogMapper logs = mock(JobLogMapper.class);
        Clock clock = mock(Clock.class);
        when(clock.withZone(ZoneId.of("Asia/Shanghai"))).thenReturn(clock);
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Shanghai"));
        // 第二次取时会跨日，确保同一响应的窗口和今日指标不会分属两天。
        when(clock.instant()).thenReturn(Instant.parse("2026-12-31T15:59:59Z"),
                Instant.parse("2026-12-31T16:00:00Z"));
        when(jobs.selectCount(any())).thenReturn(10L, 7L);
        when(logs.selectCount(any())).thenReturn(4L);
        when(logs.selectDailyCounts(any(), any())).thenReturn(List.of());

        var result = new SchedulerHomeService(jobs, logs).summary(clock);

        assertEquals(10, result.getTotalJobs());
        assertEquals(7, result.getEnabledJobs());
        assertEquals(3, result.getPausedJobs());
        assertEquals(4, result.getRunningExecutions());
        verify(clock).instant();
        verify(jobs, times(2)).selectCount(any());
        verify(logs).selectCount(any());
        verify(logs).selectDailyCounts(LocalDate.of(2026, 12, 25).atStartOfDay(),
                LocalDate.of(2027, 1, 1).atStartOfDay());
        verifyNoMoreInteractions(jobs, logs);
    }

    @Test
    void configuredBusinessDateIsIndependentOfJvmDefaultZone() {
        TimeZone originalZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            JobMapper jobs = mock(JobMapper.class);
            JobLogMapper logs = mock(JobLogMapper.class);
            when(logs.selectDailyCounts(any(), any())).thenReturn(List.of());
            var result = new SchedulerHomeService(jobs, logs).summary(
                    Clock.fixed(Instant.parse("2026-12-31T16:00:00Z"), ZoneId.systemDefault()));
            assertEquals(LocalDate.of(2027, 1, 1), result.getTrends().getLast().getDate());
            verify(logs).selectDailyCounts(LocalDate.of(2026, 12, 26).atStartOfDay(),
                    LocalDate.of(2027, 1, 2).atStartOfDay());
        } finally {
            TimeZone.setDefault(originalZone);
        }
    }

    @Test
    void beijingMidnightMonthEndAndYearEndKeepSevenDaysAndAllStatusTotals() {
        for (String instant : List.of("2026-12-31T15:59:59Z", "2026-12-31T16:00:00Z", "2026-02-28T16:00:00Z")) {
            Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
            LocalDate today = LocalDate.now(clock.withZone(ZoneId.of("Asia/Shanghai")));
            JobMapper jobs = mock(JobMapper.class);
            JobLogMapper logs = mock(JobLogMapper.class);
            List<SchedulerDailyCountVO> counts = new ArrayList<>();
            for (String status : List.of("SUCCESS", "FAILED", "SKIPPED", "RUNNING", "UNKNOWN")) {
                var count = new SchedulerDailyCountVO();
                count.setDate(today);
                count.setStatus(status);
                count.setCount(3);
                counts.add(count);
            }
            when(logs.selectDailyCounts(any(), any())).thenReturn(counts);
            var result = new SchedulerHomeService(jobs, logs).summary(clock);
            verify(logs).selectDailyCounts(today.minusDays(6).atStartOfDay(), today.plusDays(1).atStartOfDay());
            assertEquals(15, result.getTodayExecutions());
            assertEquals(3, result.getTodayFailedExecutions());
            assertEquals(7, result.getTrends().size());
            for (int offset = 0; offset < 7; offset++) {
                var trend = result.getTrends().get(offset);
                assertEquals(today.minusDays(6 - offset), trend.getDate());
                assertEquals(offset == 6 ? 3 : 0, trend.getSuccess());
                assertEquals(offset == 6 ? 3 : 0, trend.getFailed());
                assertEquals(offset == 6 ? 3 : 0, trend.getSkipped());
            }
            when(logs.selectDailyCounts(any(), any())).thenReturn(List.of());
            var empty = new SchedulerHomeService(jobs, logs).summary(clock);
            assertEquals(0, empty.getTodayExecutions());
            assertEquals(7, empty.getTrends().size());
            assertTrue(empty.getTrends().stream().allMatch(trend -> trend.getSuccess() + trend.getFailed() + trend.getSkipped() == 0));
        }
    }
}
