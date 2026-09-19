package sm.domain.sys.monitor.thread.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.thread.model.vo.ThreadDiagnosticVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 基于 JDK ThreadMXBean 采集本机平台线程，不依赖 Java Agent 或外部命令。 */
@Component
class ThreadDiagnosticAccessor {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    @Value("${smart-manage.system.runtime.instance-id}")
    private String instanceId;

    ThreadDiagnosticVO list() {
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadBean.getAllThreadIds(), 0);
        return buildResult(threadInfos, Set.of(), null, Map.of());
    }

    ThreadDiagnosticVO detail(long threadId, int maxDepth) {
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(new long[]{threadId}, true, true, maxDepth);
        if (threadInfos.length == 0 || threadInfos[0] == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "目标线程不存在或已经结束");
        }
        return buildResult(threadInfos, deadlockedThreadIds(), null, Map.of());
    }

    ThreadDiagnosticVO dump(int maxDepth) {
        ThreadInfo[] threadInfos = threadBean.dumpAllThreads(
                threadBean.isObjectMonitorUsageSupported(),
                threadBean.isSynchronizerUsageSupported(),
                maxDepth);
        return buildResult(threadInfos, deadlockedThreadIds(), null, Map.of());
    }

    ThreadDiagnosticVO deadlocks(int maxDepth) {
        Set<Long> deadlockedIds = deadlockedThreadIds();
        if (deadlockedIds.isEmpty()) {
            return buildResult(new ThreadInfo[0], deadlockedIds, null, Map.of());
        }
        long[] threadIds = deadlockedIds.stream().mapToLong(Long::longValue).toArray();
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadIds, true, true, maxDepth);
        return buildResult(threadInfos, deadlockedIds, null, Map.of());
    }

    ThreadDiagnosticVO hot(int sampleMillis, int limit, int maxDepth) {
        if (!threadBean.isThreadCpuTimeSupported()) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "当前 JVM 不支持线程 CPU 时间采样");
        }
        if (!threadBean.isThreadCpuTimeEnabled()) {
            threadBean.setThreadCpuTimeEnabled(true);
        }
        Map<Long, Long> before = threadCpuTimes();
        try {
            Thread.sleep(sampleMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultEnum.SERVER_ERROR, "线程热点采样被中断");
        }
        Map<Long, Long> after = threadCpuTimes();
        List<Map.Entry<Long, Long>> deltas = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : after.entrySet()) {
            long delta = entry.getValue() - before.getOrDefault(entry.getKey(), entry.getValue());
            if (delta >= 0) {
                deltas.add(Map.entry(entry.getKey(), delta));
            }
        }
        deltas.sort(Map.Entry.<Long, Long>comparingByValue().reversed());
        long[] selectedIds = deltas.stream().limit(limit).mapToLong(Map.Entry::getKey).toArray();
        Map<Long, Double> cpuUsage = new HashMap<>();
        for (Map.Entry<Long, Long> entry : deltas.stream().limit(limit).toList()) {
            cpuUsage.put(entry.getKey(), Math.min(100D, entry.getValue() / (sampleMillis * 1_000_000D) * 100D));
        }
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(selectedIds, false, false, maxDepth);
        return buildResult(threadInfos, deadlockedThreadIds(), sampleMillis, cpuUsage);
    }

    private Map<Long, Long> threadCpuTimes() {
        Map<Long, Long> result = new HashMap<>();
        for (long threadId : threadBean.getAllThreadIds()) {
            long cpuTime = threadBean.getThreadCpuTime(threadId);
            if (cpuTime >= 0) {
                result.put(threadId, cpuTime);
            }
        }
        return result;
    }

    private Set<Long> deadlockedThreadIds() {
        long[] threadIds = threadBean.isSynchronizerUsageSupported()
                ? threadBean.findDeadlockedThreads()
                : threadBean.findMonitorDeadlockedThreads();
        if (threadIds == null) {
            return Set.of();
        }
        Set<Long> result = new HashSet<>();
        Arrays.stream(threadIds).forEach(result::add);
        return result;
    }

    private ThreadDiagnosticVO buildResult(ThreadInfo[] threadInfos, Set<Long> deadlockedIds,
                                           Integer sampleMillis, Map<Long, Double> cpuUsage) {
        List<ThreadDiagnosticVO.ThreadItem> threads = Arrays.stream(threadInfos)
                .filter(java.util.Objects::nonNull)
                .map(threadInfo -> toItem(threadInfo, deadlockedIds, cpuUsage.get(threadInfo.getThreadId())))
                .sorted(Comparator.comparing((ThreadDiagnosticVO.ThreadItem item) ->
                        item.getCpuUsage() == null ? -1D : item.getCpuUsage()).reversed()
                        .thenComparing(ThreadDiagnosticVO.ThreadItem::getName))
                .toList();
        ThreadDiagnosticVO result = new ThreadDiagnosticVO();
        result.setInstanceId(instanceId);
        result.setSampleTime(TIME_FORMATTER.format(LocalDateTime.now()));
        result.setSampleMillis(sampleMillis);
        result.setThreads(threads);
        return result;
    }

    private ThreadDiagnosticVO.ThreadItem toItem(ThreadInfo threadInfo, Set<Long> deadlockedIds, Double cpuUsage) {
        ThreadDiagnosticVO.ThreadItem result = new ThreadDiagnosticVO.ThreadItem();
        result.setId(threadInfo.getThreadId());
        result.setName(threadInfo.getThreadName());
        result.setState(threadInfo.getThreadState().name());
        result.setDaemon(threadInfo.isDaemon());
        result.setPriority(threadInfo.getPriority());
        result.setCpuUsage(cpuUsage);
        result.setBlockedCount(threadInfo.getBlockedCount());
        result.setWaitedCount(threadInfo.getWaitedCount());
        result.setLockName(threadInfo.getLockName());
        result.setLockOwnerId(threadInfo.getLockOwnerId() < 0 ? null : threadInfo.getLockOwnerId());
        result.setLockOwnerName(threadInfo.getLockOwnerName());
        result.setDeadlocked(deadlockedIds.contains(threadInfo.getThreadId()));
        result.setStackTrace(Arrays.stream(threadInfo.getStackTrace()).map(StackTraceElement::toString).toList());
        result.setLockedMonitors(Arrays.stream(threadInfo.getLockedMonitors()).map(MonitorInfo::toString).toList());
        result.setLockedSynchronizers(Arrays.stream(threadInfo.getLockedSynchronizers()).map(LockInfo::toString).toList());
        return result;
    }
}
