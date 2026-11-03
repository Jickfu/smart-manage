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
