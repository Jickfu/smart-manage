package sm.domain.sys.scheduler.job;

import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemLogLifecycleSettingsTests {

    @Test
    void archiveParametersMustBeCompleteAndWithinBounds() throws JobExecutionException {
        JobDataMap jobDataMap = new JobDataMap(Map.of(
                "loginLogHotDays", 180,
                "operateLogHotDays", 180,
                "sqlLogHotDays", 180,
                "scriptLogHotDays", 180,
                "jobLogHotDays", 90,
                "maxPartitionsPerRun", 12));

        SystemLogLifecycleSettings settings = SystemLogLifecycleSettings.archive(jobDataMap);

        assertEquals(180, settings.retentionDays(SystemLogPartitionDefinition.LOGIN));
        assertEquals(90, settings.retentionDays(SystemLogPartitionDefinition.JOB));
        assertEquals(12, settings.maxPartitionsPerRun());
    }

    @Test
    void missingParameterIsRejected() {
        JobDataMap jobDataMap = new JobDataMap();

        assertThrows(JobExecutionException.class, () -> SystemLogLifecycleSettings.archive(jobDataMap));
    }

    @Test
    void unknownParameterIsRejected() {
        JobDataMap jobDataMap = purgeParameters();
        jobDataMap.put("tableName", "t_sys_user");

        assertThrows(JobExecutionException.class, () -> SystemLogLifecycleSettings.purge(jobDataMap));
    }

    @Test
    void excessiveBatchLimitIsRejected() {
        JobDataMap jobDataMap = purgeParameters();
        jobDataMap.put("maxPartitionsPerRun", 61);

        assertThrows(JobExecutionException.class, () -> SystemLogLifecycleSettings.purge(jobDataMap));
    }

    @Test
    void decimalParameterIsRejectedEvenWhenMathematicallyIntegral() {
        JobDataMap jobDataMap = purgeParameters();
        jobDataMap.put("jobLogRetentionDays", 365.0D);

        assertThrows(JobExecutionException.class, () -> SystemLogLifecycleSettings.purge(jobDataMap));
    }

    private static JobDataMap purgeParameters() {
        return new JobDataMap(Map.of(
                "loginLogRetentionDays", 1095,
                "operateLogRetentionDays", 1095,
                "sqlLogRetentionDays", 730,
                "scriptLogRetentionDays", 730,
                "jobLogRetentionDays", 365,
                "maxPartitionsPerRun", 12));
    }
}
