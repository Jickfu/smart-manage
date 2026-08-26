package sm.domain.sys.monitor.runtime.service;

import com.alibaba.druid.pool.DruidDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.lang.management.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.stereotype.Component;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.runtime.model.vo.InstanceSnapshotVO;

/** JVM/应用指标提供者；只由唯一采样器推进计数器差值。 */
@Component
@RequiredArgsConstructor
class ApplicationMetricsProvider {
  private final HealthEndpoint healthEndpoint;
  private final DruidDataSource dataSource;
  private final MeterRegistry meterRegistry;
  private final MonitorInstanceRegistry instanceRegistry;
  private final MonitorCollectorWarningLogger warningLogger;
  private HttpCounters previousHttpCounters;

  @Value("${smart-manage.instance-id}")
  private String instanceId;

  synchronized InstanceSnapshotVO collect(Instant sampleTime) {
    InstanceSnapshotVO result = new InstanceSnapshotVO();
    result.setInstanceId(instanceId);
    result.setHostId(instanceRegistry.currentHostId());
    result.setSampleTime(sampleTime);
    result.setRuntime(safely("instance.runtime", this::runtime, runtimeUnavailable()));
    result.setCpu(safely("instance.cpu", this::cpu, new InstanceSnapshotVO.CpuInfo()));
    result.setMemory(safely("instance.memory", this::memory, new InstanceSnapshotVO.MemoryInfo()));
    result.setThreads(safely("instance.threads", this::threads, threadsUnavailable()));
    result.setGc(safely("instance.gc", this::gc, List.of()));
    result.setDataSource(
        safely("instance.datasource", this::pool, new InstanceSnapshotVO.DataSourceInfo()));
    result.setHttp(safely("instance.http", this::http, new InstanceSnapshotVO.HttpInfo()));
    result.setHealth(safely("instance.health", this::health, healthUnavailable()));
    return result;
  }

  private InstanceSnapshotVO.RuntimeInfo runtime() {
    RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
    var result = new InstanceSnapshotVO.RuntimeInfo();
    result.setCollectorAvailable(true);
    result.setJavaVersion(System.getProperty("java.version"));
    result.setJavaVendor(System.getProperty("java.vendor"));
    result.setVmName(bean.getVmName());
    result.setStartTime(Instant.ofEpochMilli(bean.getStartTime()));
    result.setUptimeMs(bean.getUptime());
    result.setProcessors(Runtime.getRuntime().availableProcessors());
    return result;
  }

  private InstanceSnapshotVO.CpuInfo cpu() {
    var result = new InstanceSnapshotVO.CpuInfo();
    var bean = ManagementFactory.getOperatingSystemMXBean();
    if (bean instanceof com.sun.management.OperatingSystemMXBean extended)
      result.setProcessUsage(valid(extended.getProcessCpuLoad()));
    return result;
  }

  private InstanceSnapshotVO.MemoryInfo memory() {
    MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
    var result = new InstanceSnapshotVO.MemoryInfo();
    result.setCollectorAvailable(true);
    result.setHeapUsed(bean.getHeapMemoryUsage().getUsed());
    result.setHeapCommitted(bean.getHeapMemoryUsage().getCommitted());
    result.setHeapMax(bean.getHeapMemoryUsage().getMax());
    result.setNonHeapUsed(bean.getNonHeapMemoryUsage().getUsed());
    result.setNonHeapCommitted(bean.getNonHeapMemoryUsage().getCommitted());
    return result;
  }

  private InstanceSnapshotVO.ThreadInfo threads() {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (Thread.State state : Thread.State.values()) counts.put(state.name(), 0);
    for (java.lang.management.ThreadInfo info : bean.getThreadInfo(bean.getAllThreadIds(), 0))
      if (info != null) counts.merge(info.getThreadState().name(), 1, Integer::sum);
    var result = new InstanceSnapshotVO.ThreadInfo();
    result.setCollectorAvailable(true);
    result.setLive(bean.getThreadCount());
    result.setDaemon(bean.getDaemonThreadCount());
    result.setPeak(bean.getPeakThreadCount());
    result.setStateCounts(counts);
    return result;
  }

  private List<InstanceSnapshotVO.GcInfo> gc() {
    List<InstanceSnapshotVO.GcInfo> result = new ArrayList<>();
    for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
      var item = new InstanceSnapshotVO.GcInfo();
      item.setName(bean.getName());
      item.setCollectionCount(bean.getCollectionCount());
      item.setCollectionTimeMs(bean.getCollectionTime());
      result.add(item);
    }
    return result;
  }

  private InstanceSnapshotVO.DataSourceInfo pool() {
    var result = new InstanceSnapshotVO.DataSourceInfo();
    result.setCollectorAvailable(true);
    result.setActive(dataSource.getActiveCount());
    result.setIdle(dataSource.getPoolingCount());
    result.setMaxActive(dataSource.getMaxActive());
    result.setWaiting(dataSource.getWaitThreadCount());
    result.setConnectCount(dataSource.getConnectCount());
    result.setErrorCount(dataSource.getErrorCount());
    return result;
  }

  private InstanceSnapshotVO.HttpInfo http() {
    Timer aggregate = meterRegistry.find(AggregateHttpMetricsFilter.METER_NAME).timer();
    long requests = aggregate == null ? 0 : aggregate.count();
    long client = 0, server = 0;
    for (Timer timer : meterRegistry.find("http.server.requests").timers()) {
      String status = timer.getId().getTag("status");
      if (status != null && status.startsWith("4")) client += timer.count();
      if (status != null && status.startsWith("5")) server += timer.count();
    }
    long now = System.nanoTime();
    var result = new InstanceSnapshotVO.HttpInfo();
    if (aggregate != null)
      for (var percentile : aggregate.takeSnapshot().percentileValues()) {
        if (Math.abs(percentile.percentile() - .95) < .001)
          result.setP95Ms(percentile.value(TimeUnit.MILLISECONDS));
        if (Math.abs(percentile.percentile() - .99) < .001)
          result.setP99Ms(percentile.value(TimeUnit.MILLISECONDS));
      }
    if (previousHttpCounters != null) {
      double seconds = (now - previousHttpCounters.nanoTime) / 1_000_000_000d;
      result.setRequestRate(
          OshiHostMetricsProvider.rate(requests, previousHttpCounters.requests, seconds));
      result.setClientErrorRate(
          OshiHostMetricsProvider.rate(client, previousHttpCounters.clientErrors, seconds));
      result.setServerErrorRate(
          OshiHostMetricsProvider.rate(server, previousHttpCounters.serverErrors, seconds));
    }
    previousHttpCounters = new HttpCounters(now, requests, client, server);
    return result;
  }

  private InstanceSnapshotVO.HealthInfo health() {
    var descriptor = healthEndpoint.health();
    var result = new InstanceSnapshotVO.HealthInfo();
    result.setCollectorAvailable(true);
    result.setStatus(descriptor.getStatus().getCode());
    List<InstanceSnapshotVO.HealthComponent> components = new ArrayList<>();
    if (descriptor instanceof CompositeHealthDescriptor composite)
      composite
          .getComponents()
          .forEach(
              (name, item) -> {
                var component = new InstanceSnapshotVO.HealthComponent();
                component.setName(name);
                component.setStatus(item.getStatus().getCode());
                components.add(component);
              });
    result.setComponents(components);
    return result;
  }

  private Double valid(double value) {
    return Double.isFinite(value) && value >= 0 ? value : null;
  }

  private <T> T safely(String collector, java.util.function.Supplier<T> supplier, T fallback) {
    try {
      return supplier.get();
    } catch (Exception exception) {
      warningLogger.warn(collector, instanceId, exception);
      return fallback;
    }
  }

  private InstanceSnapshotVO.RuntimeInfo runtimeUnavailable() {
    InstanceSnapshotVO.RuntimeInfo value = new InstanceSnapshotVO.RuntimeInfo();
    value.setJavaVersion("unknown");
    value.setJavaVendor("unknown");
    value.setVmName("unknown");
    return value;
  }

  private InstanceSnapshotVO.ThreadInfo threadsUnavailable() {
    InstanceSnapshotVO.ThreadInfo value = new InstanceSnapshotVO.ThreadInfo();
    value.setStateCounts(Map.of());
    return value;
  }

  private InstanceSnapshotVO.HealthInfo healthUnavailable() {
    InstanceSnapshotVO.HealthInfo value = new InstanceSnapshotVO.HealthInfo();
    value.setStatus("UNKNOWN");
    value.setComponents(List.of());
    return value;
  }

  private record HttpCounters(long nanoTime, long requests, long clientErrors, long serverErrors) {}
}
