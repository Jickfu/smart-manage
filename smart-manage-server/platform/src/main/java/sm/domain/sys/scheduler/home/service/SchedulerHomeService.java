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

import java.time.LocalDate;
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
        long totalJobs = jobMapper.selectCount(new LambdaQueryWrapper<>());
        long enabledJobs = jobMapper.selectCount(new LambdaQueryWrapper<JobEntity>()
                .eq(JobEntity::getStatus, JobStatus.ENABLED.name()));
        LocalDate today = LocalDate.now();
        List<JobLogEntity> logs = jobLogMapper.selectList(new LambdaQueryWrapper<JobLogEntity>()
                .ge(JobLogEntity::getStartTime, today.minusDays(6).atStartOfDay()));
        Map<LocalDate, List<JobLogEntity>> logsByDate = logs.stream()
                .filter(log -> log.getStartTime() != null)
                .collect(Collectors.groupingBy(log -> log.getStartTime().toLocalDate()));
        List<SchedulerTrendVO> trends = IntStream.rangeClosed(0, 6)
                .mapToObj(offset -> today.minusDays(6L - offset))
                .map(date -> assembleTrend(date, logsByDate.getOrDefault(date, List.of())))
                .toList();
        List<JobLogEntity> todayLogs = logsByDate.getOrDefault(today, List.of());
        return SchedulerSummaryVO.builder()
                .totalJobs(totalJobs)
                .enabledJobs(enabledJobs)
                .pausedJobs(totalJobs - enabledJobs)
                .runningExecutions(jobLogMapper.selectCount(new LambdaQueryWrapper<JobLogEntity>()
                        .eq(JobLogEntity::getStatus, JobExecutionStatus.RUNNING.name())))
                .todayExecutions(todayLogs.size())
                .todayFailedExecutions(countStatus(todayLogs, JobExecutionStatus.FAILED))
                .trends(trends)
                .build();
    }

    private SchedulerTrendVO assembleTrend(LocalDate date, List<JobLogEntity> logs) {
        return new SchedulerTrendVO(
                date,
                countStatus(logs, JobExecutionStatus.SUCCESS),
                countStatus(logs, JobExecutionStatus.FAILED),
                countStatus(logs, JobExecutionStatus.SKIPPED));
    }

    private long countStatus(List<JobLogEntity> logs, JobExecutionStatus status) {
        return logs.stream().filter(log -> status.name().equals(log.getStatus())).count();
    }
}
