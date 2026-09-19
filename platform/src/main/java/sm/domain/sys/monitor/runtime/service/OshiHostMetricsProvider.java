package sm.domain.sys.monitor.runtime.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import sm.domain.sys.monitor.common.service.MonitorInstanceRegistry;
import sm.domain.sys.monitor.runtime.model.vo.HostSnapshotVO;

/** 采集 OS Host 指标；只由 MonitorSnapshotSampler 在单一采样链调用。 */
@Component
@RequiredArgsConstructor
class OshiHostMetricsProvider {
  private final MonitorInstanceRegistry instanceRegistry;
  private final MonitorCollectorWarningLogger warningLogger;
  private final SystemInfo systemInfo = new SystemInfo();
  private long[] previousCpuTicks;
  private IoCounters previousIoCounters;

  synchronized HostSnapshotVO collect(Instant sampleTime) {
    HostSnapshotVO snapshot = new HostSnapshotVO();
    snapshot.setHostId(instanceRegistry.currentHostId());
    snapshot.setSampleTime(sampleTime);
    HostSnapshotVO.OsInfo os = new HostSnapshotVO.OsInfo();
    snapshot.setOs(os);
    String hostId = snapshot.getHostId();
    try {
      var operatingSystem = systemInfo.getOperatingSystem();
      snapshot.setHostname(operatingSystem.getNetworkParams().getHostName());
      snapshot.setUptimeMs(operatingSystem.getSystemUptime() * 1000L);
      os.setName(operatingSystem.getFamily());
      os.setVersion(operatingSystem.getVersionInfo().getVersion());
      os.setArch(System.getProperty("os.arch"));
    } catch (Exception exception) {
      warningLogger.warn("host.metadata", hostId, exception);
    }
    snapshot.setCpu(safely("host.cpu", hostId, this::cpu, new HostSnapshotVO.CpuInfo()));
    snapshot.setMemory(
        safely("host.memory", hostId, this::memory, new HostSnapshotVO.MemoryInfo()));
    try {
      snapshot.setFilesystems(filesystems());
      snapshot.setFilesystemsAvailable(true);
    } catch (Exception exception) {
      warningLogger.warn("host.filesystem", hostId, exception);
      snapshot.setFilesystems(java.util.List.of());
    }
    snapshot.setIo(safely("host.io", hostId, () -> io(hostId), new HostSnapshotVO.IoInfo()));
    return snapshot;
  }

  private HostSnapshotVO.CpuInfo cpu() {
    var processor = systemInfo.getHardware().getProcessor();
    long[] currentTicks = processor.getSystemCpuLoadTicks();
    HostSnapshotVO.CpuInfo result = new HostSnapshotVO.CpuInfo();
    if (previousCpuTicks != null)
      result.setUsage(valid(processor.getSystemCpuLoadBetweenTicks(previousCpuTicks)));
    previousCpuTicks = currentTicks;
    result.setLoadAverage(valid(ManagementFactoryHolder.LOAD.getSystemLoadAverage()));
    return result;
  }

  private HostSnapshotVO.MemoryInfo memory() {
    var hardwareMemory = systemInfo.getHardware().getMemory();
    HostSnapshotVO.MemoryInfo result = new HostSnapshotVO.MemoryInfo();
    result.setCollectorAvailable(true);
    result.setTotal(hardwareMemory.getTotal());
    result.setAvailable(hardwareMemory.getAvailable());
    result.setSwapTotal(hardwareMemory.getVirtualMemory().getSwapTotal());
    result.setSwapUsed(hardwareMemory.getVirtualMemory().getSwapUsed());
    return result;
  }

  private List<HostSnapshotVO.FilesystemInfo> filesystems() {
    List<HostSnapshotVO.FilesystemInfo> result = new ArrayList<>();
    for (var store : systemInfo.getOperatingSystem().getFileSystem().getFileStores()) {
      try {
        if (store.getTotalSpace() <= 0 || isSynthetic(store.getType())) continue;
        HostSnapshotVO.FilesystemInfo item = new HostSnapshotVO.FilesystemInfo();
        long used = Math.max(0, store.getTotalSpace() - store.getUsableSpace());
        item.setName(store.getName());
        item.setMount(store.getMount());
        item.setType(store.getType());
        item.setTotal(store.getTotalSpace());
        item.setUsed(used);
        item.setAvailable(store.getUsableSpace());
        item.setUsage((double) used / store.getTotalSpace());
        result.add(item);
      } catch (Exception exception) {
        warningLogger.warn(
            "host.filesystem." + String.valueOf(store.getMount()),
            instanceRegistry.currentHostId(),
            exception);
      }
    }
    return result;
  }

  private boolean isSynthetic(String type) {
    String normalized = type == null ? "" : type.toLowerCase(java.util.Locale.ROOT);
    return normalized.equals("overlay")
        || normalized.equals("tmpfs")
        || normalized.equals("squashfs");
  }

  private HostSnapshotVO.IoInfo io(String hostId) {
    long read = 0, write = 0, receive = 0, transmit = 0;
    boolean diskComplete = true;
    boolean networkComplete = true;
    for (HWDiskStore disk : systemInfo.getHardware().getDiskStores()) {
      try {
        disk.updateAttributes();
        read += disk.getReadBytes();
        write += disk.getWriteBytes();
      } catch (Exception exception) {
        diskComplete = false;
        warningLogger.warn("host.disk." + disk.getName(), hostId, exception);
      }
    }
    for (NetworkIF network : systemInfo.getHardware().getNetworkIFs()) {
      try {
        network.updateAttributes();
        receive += network.getBytesRecv();
        transmit += network.getBytesSent();
      } catch (Exception exception) {
        networkComplete = false;
        warningLogger.warn("host.network." + network.getName(), hostId, exception);
      }
    }
    long nanoTime = System.nanoTime();
    HostSnapshotVO.IoInfo result = new HostSnapshotVO.IoInfo();
    result.setCollectorAvailable(true);
    result.setDiskReadBytes(read);
    result.setDiskWriteBytes(write);
    result.setNetworkReceiveBytes(receive);
    result.setNetworkTransmitBytes(transmit);
    if (previousIoCounters != null) {
      double seconds = (nanoTime - previousIoCounters.nanoTime) / 1_000_000_000d;
      if (seconds > 0 && diskComplete && previousIoCounters.diskComplete) {
        result.setDiskReadBytesPerSecond(rate(read, previousIoCounters.read, seconds));
        result.setDiskWriteBytesPerSecond(rate(write, previousIoCounters.write, seconds));
      }
      if (seconds > 0 && networkComplete && previousIoCounters.networkComplete) {
        result.setNetworkReceiveBytesPerSecond(rate(receive, previousIoCounters.receive, seconds));
        result.setNetworkTransmitBytesPerSecond(
            rate(transmit, previousIoCounters.transmit, seconds));
      }
    }
    previousIoCounters =
        new IoCounters(nanoTime, read, write, receive, transmit, diskComplete, networkComplete);
    return result;
  }

  static Double rate(long current, long previous, double seconds) {
    return current >= previous && seconds > 0 ? (current - previous) / seconds : null;
  }

  private Double valid(double value) {
    return Double.isFinite(value) && value >= 0 ? value : null;
  }

  private <T> T safely(
      String collector, String hostId, java.util.function.Supplier<T> supplier, T fallback) {
    try {
      return supplier.get();
    } catch (Exception exception) {
      warningLogger.warn(collector, hostId, exception);
      return fallback;
    }
  }

  private record IoCounters(
      long nanoTime,
      long read,
      long write,
      long receive,
      long transmit,
      boolean diskComplete,
      boolean networkComplete) {}

  private static final class ManagementFactoryHolder {
    private static final java.lang.management.OperatingSystemMXBean LOAD =
        java.lang.management.ManagementFactory.getOperatingSystemMXBean();
  }
}
