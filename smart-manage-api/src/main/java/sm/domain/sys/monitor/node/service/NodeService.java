package sm.domain.sys.monitor.node.service;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;
import sm.domain.sys.monitor.node.model.vo.NodeInfoVO;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.common.service.MonitorRoutingGateway;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 当前应用实例运行监控的唯一公开业务入口。 */
@Service
@RequiredArgsConstructor
public class NodeService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HealthEndpoint healthEndpoint;
    private final DruidDataSource dataSource;
    private final MonitorInstanceRegistry instanceRegistry;
    private final MonitorRoutingGateway routingGateway;
    private final SystemInfo systemInfo = new SystemInfo();

    @Value("${smart-manage.instance-id}")
    private String instanceId;

    public List<MonitorInstanceVO> instances() {
        return instanceRegistry.listOnline();
    }

    public NodeInfoVO snapshot(String targetInstanceId) {
        MonitorInstanceRegistry.RegisteredInstance instance = instanceRegistry.require(targetInstanceId);
        if (instanceRegistry.isCurrent(instance.getInstanceId())) {
            return localSnapshot();
        }
        return routingGateway.get(instance, "/sys/monitor/internal/node/snapshot", NodeInfoVO.class);
    }

    /** 仅供目标节点内部接口采集本机快照，禁止再次执行实例路由。 */
    public NodeInfoVO localSnapshot() {
        NodeInfoVO result = new NodeInfoVO();
        result.setInstanceId(instanceId);
        result.setSampleTime(TIME_FORMATTER.format(java.time.LocalDateTime.now()));
        result.setRuntime(buildRuntimeInfo());
        result.setOs(buildOsInfo());
        result.setCpu(buildCpuInfo());
        result.setMemory(buildMemoryInfo());
        result.setDisk(buildDiskInfo());
        result.setThreads(buildThreadInfo());
        result.setGc(buildGcInfo());
        result.setDataSource(buildDataSourceInfo());
        result.setHealth(buildHealthInfo());
        return result;
    }

    private NodeInfoVO.RuntimeInfo buildRuntimeInfo() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        NodeInfoVO.RuntimeInfo result = new NodeInfoVO.RuntimeInfo();
        result.setJavaVersion(System.getProperty("java.version"));
        result.setJavaVendor(System.getProperty("java.vendor"));
        result.setVmName(runtime.getVmName());
        result.setStartTime(TIME_FORMATTER.format(Instant.ofEpochMilli(runtime.getStartTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime()));
        result.setUptimeMs(runtime.getUptime());
        result.setProcessors(Runtime.getRuntime().availableProcessors());
        return result;
    }

    private NodeInfoVO.OsInfo buildOsInfo() {
        var operatingSystem = systemInfo.getOperatingSystem();
        NodeInfoVO.OsInfo result = new NodeInfoVO.OsInfo();
        result.setName(operatingSystem.getFamily());
        result.setVersion(operatingSystem.getVersionInfo().getVersion());
        result.setArch(System.getProperty("os.arch"));
        return result;
    }

    private NodeInfoVO.CpuInfo buildCpuInfo() {
        NodeInfoVO.CpuInfo result = new NodeInfoVO.CpuInfo();
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        result.setLoadAverage(normalizeMetric(bean.getSystemLoadAverage()));
        if (bean instanceof com.sun.management.OperatingSystemMXBean extendedBean) {
            result.setSystemUsage(normalizeMetric(extendedBean.getCpuLoad()));
            result.setProcessUsage(normalizeMetric(extendedBean.getProcessCpuLoad()));
        }
        return result;
    }

    private NodeInfoVO.MemoryInfo buildMemoryInfo() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        NodeInfoVO.MemoryInfo result = new NodeInfoVO.MemoryInfo();
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
        return result;
    }

    private NodeInfoVO.DiskInfo buildDiskInfo() {
        String applicationDirectory = System.getProperty("user.dir").toLowerCase();
        OSFileStore matched = systemInfo.getOperatingSystem().getFileSystem().getFileStores().stream()
                .filter(store -> applicationDirectory.startsWith(store.getMount().toLowerCase()))
                .max(Comparator.comparingInt(store -> store.getMount().length()))
                .orElse(null);
        NodeInfoVO.DiskInfo result = new NodeInfoVO.DiskInfo();
        if (matched != null) {
            result.setMount(matched.getMount());
            result.setTotal(matched.getTotalSpace());
            result.setAvailable(matched.getUsableSpace());
            result.setUsed(Math.max(0, matched.getTotalSpace() - matched.getUsableSpace()));
        }
        return result;
    }

    private NodeInfoVO.ThreadInfo buildThreadInfo() {
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
        NodeInfoVO.ThreadInfo result = new NodeInfoVO.ThreadInfo();
        result.setLive(threadBean.getThreadCount());
        result.setDaemon(threadBean.getDaemonThreadCount());
        result.setPeak(threadBean.getPeakThreadCount());
        result.setStateCounts(serializedCounts);
        return result;
    }

    private List<NodeInfoVO.GcInfo> buildGcInfo() {
        List<NodeInfoVO.GcInfo> result = new ArrayList<>();
        for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            NodeInfoVO.GcInfo item = new NodeInfoVO.GcInfo();
            item.setName(collector.getName());
            item.setCollectionCount(collector.getCollectionCount());
            item.setCollectionTimeMs(collector.getCollectionTime());
            result.add(item);
        }
        return result;
    }

    private NodeInfoVO.DataSourceInfo buildDataSourceInfo() {
        NodeInfoVO.DataSourceInfo result = new NodeInfoVO.DataSourceInfo();
        result.setActive(dataSource.getActiveCount());
        result.setIdle(dataSource.getPoolingCount());
        result.setMaxActive(dataSource.getMaxActive());
        result.setWaiting(dataSource.getWaitThreadCount());
        result.setConnectCount(dataSource.getConnectCount());
        result.setErrorCount(dataSource.getErrorCount());
        return result;
    }

    private NodeInfoVO.HealthInfo buildHealthInfo() {
        HealthDescriptor health = healthEndpoint.health();
        NodeInfoVO.HealthInfo result = new NodeInfoVO.HealthInfo();
        result.setStatus(health.getStatus().getCode());
        List<NodeInfoVO.HealthComponent> components = new ArrayList<>();
        if (health instanceof CompositeHealthDescriptor composite) {
            composite.getComponents().forEach((name, descriptor) -> {
                NodeInfoVO.HealthComponent component = new NodeInfoVO.HealthComponent();
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
