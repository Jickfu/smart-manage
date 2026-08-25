package sm.domain.sys.monitor.runtime.service;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import sm.domain.sys.monitor.runtime.model.vo.RuntimeSnapshotVO;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.json.JsonMapper;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/** 当前应用实例运行监控的唯一公开业务入口。 */
@Service
@RequiredArgsConstructor
public class RuntimeMonitorService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HealthEndpoint healthEndpoint;
    private final DruidDataSource dataSource;
    private final MonitorInstanceRegistry instanceRegistry;
    private final MeterRegistry meterRegistry;
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final SystemInfo systemInfo = new SystemInfo();
    private long[] previousCpuTicks;
    private IoCounters previousIoCounters;
    private HttpCounters previousHttpCounters;

    @Value("${smart-manage.instance-id}")
    private String instanceId;

    public List<MonitorInstanceVO> instances() {
        return instanceRegistry.listOnline();
    }

    public List<Map<String,Object>> topology() {
        Map<String, MonitorInstanceVO> online = new HashMap<>();
        for (MonitorInstanceVO instance : instanceRegistry.listOnline()) online.put(instance.getInstanceId(), instance);
        List<Map<String,Object>> hosts = jdbcTemplate.queryForList("SELECT * FROM t_sys_monitor_host ORDER BY host_name,host_id");
        for (Map<String,Object> host : hosts) {
            String hostId = (String) host.get("host_id");
            List<Map<String,Object>> instances = jdbcTemplate.queryForList("SELECT * FROM t_sys_monitor_instance WHERE host_id=? ORDER BY instance_id", hostId);
            boolean telemetryAvailable = false;
            for (Map<String,Object> instance : instances) {
                MonitorInstanceVO onlineInstance = online.get(instance.get("instance_id"));
                boolean isOnline = onlineInstance != null;
                instance.put("online", isOnline); instance.put("current", isOnline && onlineInstance.isCurrent());
                telemetryAvailable |= isOnline;
            }
            host.put("status", telemetryAvailable ? "UP" : "TELEMETRY_UNAVAILABLE");
            host.put("instances", instances);
        }
        return hosts;
    }

    public List<Map<String,Object>> history(String scopeType, String scopeId, String range) {
        HistoryRange historyRange = HistoryRange.parse(range);
        if (scopeId == null || scopeId.isBlank()) throw new sm.system.exception.BizException(sm.system.response.ResultEnum.PARAM_ERROR, "监控对象不能为空");
        if ("HOST".equals(scopeType)) {
            return jdbcTemplate.queryForList("SELECT date_bin(INTERVAL '" + historyRange.bucket + "',sample_time,TIMESTAMPTZ '2000-01-01') sample_time,"
                    + "avg(cpu_usage) cpu_usage,avg(memory_used) memory_used,avg(memory_total) memory_total,avg(filesystem_used) filesystem_used,"
                    + "avg(filesystem_total) filesystem_total,avg(disk_read_rate) disk_read_rate,avg(disk_write_rate) disk_write_rate,"
                    + "avg(network_receive_rate) network_receive_rate,avg(network_transmit_rate) network_transmit_rate "
                    + "FROM t_sys_monitor_host_history WHERE host_id=? AND sample_time>=now()-INTERVAL '" + historyRange.lookback
                    + "' GROUP BY 1 ORDER BY 1", scopeId.trim());
        }
        if ("INSTANCE".equals(scopeType)) {
            return jdbcTemplate.queryForList("SELECT date_bin(INTERVAL '" + historyRange.bucket + "',sample_time,TIMESTAMPTZ '2000-01-01') sample_time,"
                    + "avg(process_cpu) process_cpu,avg(heap_used) heap_used,avg(heap_max) heap_max,avg(thread_count) thread_count,"
                    + "avg(blocked_thread_count) blocked_thread_count,avg(http_request_rate) http_request_rate,avg(http_5xx_rate) http_5xx_rate,"
                    + "avg(http_p95_ms) http_p95_ms,avg(http_p99_ms) http_p99_ms,avg(db_active) db_active,avg(db_max) db_max "
                    + "FROM t_sys_monitor_instance_history WHERE instance_id=? AND sample_time>=now()-INTERVAL '" + historyRange.lookback
                    + "' GROUP BY 1 ORDER BY 1", scopeId.trim());
        }
        throw new sm.system.exception.BizException(sm.system.response.ResultEnum.PARAM_ERROR, "监控对象类型不合法");
    }

    private enum HistoryRange {
        H1("1 hour", "1 minute"), H6("6 hours", "5 minutes"), H24("24 hours", "10 minutes"), D7("7 days", "30 minutes");
        private final String lookback; private final String bucket;
        HistoryRange(String lookback, String bucket) { this.lookback = lookback; this.bucket = bucket; }
        static HistoryRange parse(String value) {
            return switch (value == null ? "1h" : value.toLowerCase(Locale.ROOT)) {
                case "1h" -> H1; case "6h" -> H6; case "24h" -> H24; case "7d" -> D7;
                default -> throw new sm.system.exception.BizException(sm.system.response.ResultEnum.PARAM_ERROR, "历史范围仅支持 1h、6h、24h、7d");
            };
        }
    }

    public RuntimeSnapshotVO snapshot(String targetInstanceId) {
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(targetInstanceId);
        String json = redisTemplate.opsForValue().get("sm:monitor:snapshot:instance:" + instance.getInstanceId());
        if (json == null && instanceRegistry.isCurrent(instance.getInstanceId())) return localSnapshot();
        if (json == null) throw new BizException(ResultEnum.NOT_FOUND, "目标实例当前遥测快照不可用");
        try {
            return jsonMapper.readValue(json, RuntimeSnapshotVO.class);
        } catch (Exception exception) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "目标实例当前遥测快照损坏");
        }
    }

    /** 仅供目标节点内部接口采集本机快照，禁止再次执行实例路由。 */
    public RuntimeSnapshotVO localSnapshot() {
        RuntimeSnapshotVO result = new RuntimeSnapshotVO();
        result.setInstanceId(instanceId);
        // 本机采集不反向依赖 Redis 注册表，避免遥测故障阻断历史数据持久化。
        result.setHostId(instanceRegistry.currentHostId());
        result.setSampleTime(TIME_FORMATTER.format(java.time.LocalDateTime.now()));
        result.setRuntime(buildRuntimeInfo());
        result.setOs(buildOsInfo());
        result.setCpu(buildCpuInfo());
        result.setMemory(buildMemoryInfo());
        result.setFilesystems(buildFilesystemInfo());
        result.setIo(buildIoInfo());
        result.setThreads(buildThreadInfo());
        result.setGc(buildGcInfo());
        result.setDataSource(buildDataSourceInfo());
        result.setHttp(buildHttpInfo());
        result.setHealth(buildHealthInfo());
        return result;
    }

    private RuntimeSnapshotVO.RuntimeInfo buildRuntimeInfo() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        RuntimeSnapshotVO.RuntimeInfo result = new RuntimeSnapshotVO.RuntimeInfo();
        result.setJavaVersion(System.getProperty("java.version"));
        result.setJavaVendor(System.getProperty("java.vendor"));
        result.setVmName(runtime.getVmName());
        result.setStartTime(TIME_FORMATTER.format(Instant.ofEpochMilli(runtime.getStartTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime()));
        result.setUptimeMs(runtime.getUptime());
        result.setProcessors(Runtime.getRuntime().availableProcessors());
        return result;
    }

    private RuntimeSnapshotVO.OsInfo buildOsInfo() {
        var operatingSystem = systemInfo.getOperatingSystem();
        RuntimeSnapshotVO.OsInfo result = new RuntimeSnapshotVO.OsInfo();
        result.setName(operatingSystem.getFamily());
        result.setVersion(operatingSystem.getVersionInfo().getVersion());
        result.setArch(System.getProperty("os.arch"));
        return result;
    }

    private RuntimeSnapshotVO.CpuInfo buildCpuInfo() {
        RuntimeSnapshotVO.CpuInfo result = new RuntimeSnapshotVO.CpuInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] currentTicks = processor.getSystemCpuLoadTicks();
        synchronized (this) {
            if (previousCpuTicks != null) result.setSystemUsage(normalizeMetric(processor.getSystemCpuLoadBetweenTicks(previousCpuTicks)));
            previousCpuTicks = currentTicks;
        }
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        result.setLoadAverage(normalizeMetric(bean.getSystemLoadAverage()));
        if (bean instanceof com.sun.management.OperatingSystemMXBean extendedBean) {
            result.setProcessUsage(normalizeMetric(extendedBean.getProcessCpuLoad()));
        }
        return result;
    }

    private RuntimeSnapshotVO.MemoryInfo buildMemoryInfo() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        RuntimeSnapshotVO.MemoryInfo result = new RuntimeSnapshotVO.MemoryInfo();
        result.setHeapUsed(memory.getHeapMemoryUsage().getUsed());
        result.setHeapCommitted(memory.getHeapMemoryUsage().getCommitted());
        result.setHeapMax(memory.getHeapMemoryUsage().getMax());
        result.setNonHeapUsed(memory.getNonHeapMemoryUsage().getUsed());
        result.setNonHeapCommitted(memory.getNonHeapMemoryUsage().getCommitted());
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean extendedBean) {
            result.setPhysicalTotal(extendedBean.getTotalMemorySize());
            result.setPhysicalAvailable(extendedBean.getFreeMemorySize());
        }
        var virtualMemory = systemInfo.getHardware().getMemory().getVirtualMemory();
        result.setSwapTotal(virtualMemory.getSwapTotal());
        result.setSwapUsed(virtualMemory.getSwapUsed());
        return result;
    }

    private List<RuntimeSnapshotVO.FilesystemInfo> buildFilesystemInfo() {
        List<RuntimeSnapshotVO.FilesystemInfo> result = new ArrayList<>();
        for (OSFileStore store : systemInfo.getOperatingSystem().getFileSystem().getFileStores()) {
            if (store.getTotalSpace() <= 0) continue;
            RuntimeSnapshotVO.FilesystemInfo item = new RuntimeSnapshotVO.FilesystemInfo();
            item.setName(store.getName()); item.setMount(store.getMount()); item.setType(store.getType());
            item.setTotal(store.getTotalSpace()); item.setAvailable(store.getUsableSpace());
            item.setUsed(Math.max(0, store.getTotalSpace() - store.getUsableSpace()));
            result.add(item);
        }
        return result;
    }

    private RuntimeSnapshotVO.IoInfo buildIoInfo() {
        long readBytes = 0, writeBytes = 0, receiveBytes = 0, transmitBytes = 0;
        for (HWDiskStore disk : systemInfo.getHardware().getDiskStores()) {
            disk.updateAttributes(); readBytes += disk.getReadBytes(); writeBytes += disk.getWriteBytes();
        }
        for (NetworkIF network : systemInfo.getHardware().getNetworkIFs()) {
            network.updateAttributes(); receiveBytes += network.getBytesRecv(); transmitBytes += network.getBytesSent();
        }
        long now = System.nanoTime();
        IoCounters current = new IoCounters(now, readBytes, writeBytes, receiveBytes, transmitBytes);
        RuntimeSnapshotVO.IoInfo result = new RuntimeSnapshotVO.IoInfo();
        result.setDiskReadBytes(readBytes); result.setDiskWriteBytes(writeBytes);
        result.setNetworkReceiveBytes(receiveBytes); result.setNetworkTransmitBytes(transmitBytes);
        synchronized (this) {
            if (previousIoCounters != null) {
                double seconds = (now - previousIoCounters.nanoTime()) / 1_000_000_000d;
                if (seconds > 0) {
                    result.setDiskReadBytesPerSecond(rate(readBytes, previousIoCounters.diskReadBytes(), seconds));
                    result.setDiskWriteBytesPerSecond(rate(writeBytes, previousIoCounters.diskWriteBytes(), seconds));
                    result.setNetworkReceiveBytesPerSecond(rate(receiveBytes, previousIoCounters.networkReceiveBytes(), seconds));
                    result.setNetworkTransmitBytesPerSecond(rate(transmitBytes, previousIoCounters.networkTransmitBytes(), seconds));
                }
            }
            previousIoCounters = current;
        }
        return result;
    }

    private Double rate(long current, long previous, double seconds) {
        return current >= previous ? (current - previous) / seconds : null;
    }

    private record IoCounters(long nanoTime, long diskReadBytes, long diskWriteBytes,
                              long networkReceiveBytes, long networkTransmitBytes) { }

    private RuntimeSnapshotVO.ThreadInfo buildThreadInfo() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        EnumMap<Thread.State, Integer> stateCounts = new EnumMap<>(Thread.State.class);
        java.lang.management.ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadBean.getAllThreadIds(), 0);
        for (java.lang.management.ThreadInfo threadInfo : threadInfos) {
            if (threadInfo != null) {
                stateCounts.merge(threadInfo.getThreadState(), 1, Integer::sum);
            }
        }
        Map<String, Integer> serializedCounts = new LinkedHashMap<>();
        for (Thread.State state : Thread.State.values()) {
            serializedCounts.put(state.name(), stateCounts.getOrDefault(state, 0));
        }
        RuntimeSnapshotVO.ThreadInfo result = new RuntimeSnapshotVO.ThreadInfo();
        result.setLive(threadBean.getThreadCount());
        result.setDaemon(threadBean.getDaemonThreadCount());
        result.setPeak(threadBean.getPeakThreadCount());
        result.setStateCounts(serializedCounts);
        return result;
    }

    private List<RuntimeSnapshotVO.GcInfo> buildGcInfo() {
        List<RuntimeSnapshotVO.GcInfo> result = new ArrayList<>();
        for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            RuntimeSnapshotVO.GcInfo item = new RuntimeSnapshotVO.GcInfo();
            item.setName(collector.getName());
            item.setCollectionCount(collector.getCollectionCount());
            item.setCollectionTimeMs(collector.getCollectionTime());
            result.add(item);
        }
        return result;
    }

    private RuntimeSnapshotVO.DataSourceInfo buildDataSourceInfo() {
        RuntimeSnapshotVO.DataSourceInfo result = new RuntimeSnapshotVO.DataSourceInfo();
        result.setActive(dataSource.getActiveCount());
        result.setIdle(dataSource.getPoolingCount());
        result.setMaxActive(dataSource.getMaxActive());
        result.setWaiting(dataSource.getWaitThreadCount());
        result.setConnectCount(dataSource.getConnectCount());
        result.setErrorCount(dataSource.getErrorCount());
        return result;
    }

    private RuntimeSnapshotVO.HttpInfo buildHttpInfo() {
        long requestCount = 0, clientErrorCount = 0, serverErrorCount = 0;
        double p95Ms = 0, p99Ms = 0;
        for (Timer timer : meterRegistry.find("http.server.requests").timers()) {
            requestCount += timer.count();
            String status = timer.getId().getTag("status");
            if (status != null && status.startsWith("4")) clientErrorCount += timer.count();
            if (status != null && status.startsWith("5")) serverErrorCount += timer.count();
            for (var percentile : timer.takeSnapshot().percentileValues()) {
                if (Math.abs(percentile.percentile() - 0.95) < 0.001) p95Ms = Math.max(p95Ms, percentile.value(java.util.concurrent.TimeUnit.MILLISECONDS));
                if (Math.abs(percentile.percentile() - 0.99) < 0.001) p99Ms = Math.max(p99Ms, percentile.value(java.util.concurrent.TimeUnit.MILLISECONDS));
            }
        }
        long now = System.nanoTime();
        HttpCounters current = new HttpCounters(now, requestCount, clientErrorCount, serverErrorCount);
        RuntimeSnapshotVO.HttpInfo result = new RuntimeSnapshotVO.HttpInfo();
        result.setP95Ms(p95Ms > 0 ? p95Ms : null); result.setP99Ms(p99Ms > 0 ? p99Ms : null);
        synchronized (this) {
            if (previousHttpCounters != null) {
                double seconds = (now - previousHttpCounters.nanoTime()) / 1_000_000_000d;
                result.setRequestRate(rate(requestCount, previousHttpCounters.requestCount(), seconds));
                result.setClientErrorRate(rate(clientErrorCount, previousHttpCounters.clientErrorCount(), seconds));
                result.setServerErrorRate(rate(serverErrorCount, previousHttpCounters.serverErrorCount(), seconds));
            }
            previousHttpCounters = current;
        }
        return result;
    }

    private record HttpCounters(long nanoTime, long requestCount, long clientErrorCount, long serverErrorCount) { }

    private RuntimeSnapshotVO.HealthInfo buildHealthInfo() {
        HealthDescriptor health = healthEndpoint.health();
        RuntimeSnapshotVO.HealthInfo result = new RuntimeSnapshotVO.HealthInfo();
        result.setStatus(health.getStatus().getCode());
        List<RuntimeSnapshotVO.HealthComponent> components = new ArrayList<>();
        if (health instanceof CompositeHealthDescriptor composite) {
            composite.getComponents().forEach((name, descriptor) -> {
                RuntimeSnapshotVO.HealthComponent component = new RuntimeSnapshotVO.HealthComponent();
                component.setName(name);
                component.setStatus(descriptor.getStatus().getCode());
                components.add(component);
            });
        }
        result.setComponents(components);
        return result;
    }

    private Double normalizeMetric(double value) {
        return Double.isFinite(value) && value >= 0 ? value : null;
    }
}
