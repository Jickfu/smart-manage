package sm.domain.sys.scheduler.home.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.scheduler.constant.JobExecutionStatus;
import sm.domain.sys.scheduler.constant.JobStatus;
import sm.domain.sys.scheduler.mapper.JobLogMapper;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.entity.JobEntity;
import sm.domain.sys.scheduler.model.entity.JobLogEntity;
import sm.domain.sys.scheduler.model.vo.SchedulerSummaryVO;
import sm.domain.sys.scheduler.model.vo.SchedulerTrendVO;
import sm.system.time.BusinessTime;

import java.time.LocalDate;
import java.time.Clock;
import sm.domain.sys.scheduler.model.vo.SchedulerDailyCountVO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** 任务调度应用首页查询服务。 */
@Service
@RequiredArgsConstructor
public class SchedulerHomeService {

    private final JobMapper jobMapper;
    private final JobLogMapper jobLogMapper;

    public SchedulerSummaryVO summary() {
        return summary(Clock.systemUTC());
    }

    // 一次请求固定北京时间基准，避免午夜跨日及 JVM 默认时区影响。
    SchedulerSummaryVO summary(Clock clock) {
        long totalJobs = jobMapper.selectCount(new LambdaQueryWrapper<>());
        long enabledJobs = jobMapper.selectCount(new LambdaQueryWrapper<JobEntity>()
                .eq(JobEntity::getStatus, JobStatus.ENABLED.name()));
        LocalDate today = LocalDate.now(clock.withZone(BusinessTime.ZONE));
        List<SchedulerDailyCountVO> counts = jobLogMapper.selectDailyCounts(
                today.minusDays(6).atStartOfDay(), today.plusDays(1).atStartOfDay());
        Map<LocalDate, List<SchedulerDailyCountVO>> logsByDate = counts.stream()
                .collect(Collectors.groupingBy(SchedulerDailyCountVO::getDate));
        List<SchedulerTrendVO> trends = IntStream.rangeClosed(0, 6)
                .mapToObj(offset -> today.minusDays(6L - offset))
                .map(date -> assembleTrend(date, logsByDate.getOrDefault(date, List.of())))
                .toList();
        List<SchedulerDailyCountVO> todayLogs = logsByDate.getOrDefault(today, List.of());
        return SchedulerSummaryVO.builder()
                .totalJobs(totalJobs)
                .enabledJobs(enabledJobs)
                .pausedJobs(totalJobs - enabledJobs)
                .runningExecutions(jobLogMapper.selectCount(new LambdaQueryWrapper<JobLogEntity>()
                        .eq(JobLogEntity::getStatus, JobExecutionStatus.RUNNING.name())))
                .todayExecutions(todayLogs.stream().mapToLong(SchedulerDailyCountVO::getCount).sum())
                .todayFailedExecutions(countStatus(todayLogs, JobExecutionStatus.FAILED))
                .trends(trends)
                .build();
    }

    private SchedulerTrendVO assembleTrend(LocalDate date, List<SchedulerDailyCountVO> logs) {
        return new SchedulerTrendVO(
                date,
                countStatus(logs, JobExecutionStatus.SUCCESS),
                countStatus(logs, JobExecutionStatus.FAILED),
                countStatus(logs, JobExecutionStatus.SKIPPED));
    }

    private long countStatus(List<SchedulerDailyCountVO> logs, JobExecutionStatus status) {
        return logs.stream().filter(log -> status.name().equals(log.getStatus()))
                .mapToLong(SchedulerDailyCountVO::getCount).sum();
    }
}
