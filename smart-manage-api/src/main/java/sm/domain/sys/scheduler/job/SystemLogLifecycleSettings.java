package sm.domain.sys.scheduler.job;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 系统日志生命周期任务参数；缺失、未知或越界参数均拒绝执行。 */
record SystemLogLifecycleSettings(Map<SystemLogPartitionDefinition, Integer> retentionDays,
                                  int maxPartitionsPerRun) {
    private static final int MIN_RETENTION_DAYS = 1;
    private static final int MAX_RETENTION_DAYS = 36500;
    private static final int MAX_PARTITIONS_PER_RUN = 60;

    static SystemLogLifecycleSettings archive(JobDataMap jobDataMap) throws JobExecutionException {
        return parse(jobDataMap, "HotDays");
    }

    static SystemLogLifecycleSettings purge(JobDataMap jobDataMap) throws JobExecutionException {
        return parse(jobDataMap, "RetentionDays");
    }

    int retentionDays(SystemLogPartitionDefinition definition) {
        return retentionDays.get(definition);
    }

    private static SystemLogLifecycleSettings parse(JobDataMap jobDataMap, String suffix)
            throws JobExecutionException {
        Set<String> allowedKeys = new HashSet<>();
        EnumMap<SystemLogPartitionDefinition, Integer> retentionDays =
                new EnumMap<>(SystemLogPartitionDefinition.class);
        for (SystemLogPartitionDefinition definition : SystemLogPartitionDefinition.VALUES) {
            String key = definition.parameterPrefix() + suffix;
            allowedKeys.add(key);
            retentionDays.put(definition, requiredInteger(jobDataMap, key,
                    MIN_RETENTION_DAYS, MAX_RETENTION_DAYS));
        }
        allowedKeys.add("maxPartitionsPerRun");
        int maxPartitions = requiredInteger(jobDataMap, "maxPartitionsPerRun", 1, MAX_PARTITIONS_PER_RUN);
        for (String key : jobDataMap.getKeys()) {
            if (!allowedKeys.contains(key) && !key.startsWith("__") && !"smartManageJobId".equals(key)) {
                throw new JobExecutionException("存在未知的系统日志生命周期参数: " + key);
            }
        }
        return new SystemLogLifecycleSettings(Map.copyOf(retentionDays), maxPartitions);
    }

    private static int requiredInteger(JobDataMap jobDataMap, String key, int minimum, int maximum)
            throws JobExecutionException {
        Object rawValue = jobDataMap.get(key);
        if (!(rawValue instanceof Byte || rawValue instanceof Short
                || rawValue instanceof Integer || rawValue instanceof Long)) {
            throw new JobExecutionException("系统日志生命周期参数必须是整数: " + key);
        }
        Number number = (Number) rawValue;
        int value = number.intValue();
        if (number.doubleValue() != value || value < minimum || value > maximum) {
            throw new JobExecutionException(
                    "系统日志生命周期参数 " + key + " 必须在 " + minimum + "～" + maximum + " 之间");
        }
        return value;
    }
}
