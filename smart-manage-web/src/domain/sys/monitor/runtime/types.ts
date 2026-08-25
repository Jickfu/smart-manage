export interface MonitorInstance {
  instanceId: string;
  hostId: string;
  applicationVersion: string;
  startTime: string;
  lastSeenTime: string;
  current: boolean;
}
export interface RuntimeSnapshot {
  instanceId: string;
  hostId: string;
  sampleTime: string;
  runtime: {
    javaVersion: string;
    javaVendor: string;
    vmName: string;
    startTime: string;
    uptimeMs: number;
    processors: number;
  };
  os: { name: string; version: string; arch: string };
  cpu: { systemUsage?: number; processUsage?: number; loadAverage?: number };
  memory: {
    heapUsed: number;
    heapCommitted: number;
    heapMax: number;
    nonHeapUsed: number;
    nonHeapCommitted: number;
    physicalTotal: number;
    physicalAvailable: number;
    swapTotal: number;
    swapUsed: number;
  };
  filesystems: Array<{
    name: string;
    mount: string;
    type: string;
    total: number;
    used: number;
    available: number;
  }>;
  io: {
    diskReadBytes: number;
    diskWriteBytes: number;
    diskReadBytesPerSecond?: number;
    diskWriteBytesPerSecond?: number;
    networkReceiveBytes: number;
    networkTransmitBytes: number;
    networkReceiveBytesPerSecond?: number;
    networkTransmitBytesPerSecond?: number;
  };
  threads: { live: number; daemon: number; peak: number; stateCounts: Record<string, number> };
  gc: Array<{ name: string; collectionCount: number; collectionTimeMs: number }>;
  dataSource: {
    active: number;
    idle: number;
    maxActive: number;
    waiting: number;
    connectCount: number;
    errorCount: number;
  };
  http: {
    requestRate?: number;
    clientErrorRate?: number;
    serverErrorRate?: number;
    p95Ms?: number;
    p99Ms?: number;
  };
  health: { status: string; components: Array<{ name: string; status: string }> };
}
export interface TopologyInstance {
  instance_id: string;
  application_name: string;
  application_version?: string;
  last_seen_time: string;
  online: boolean;
  current: boolean;
}
export interface MonitorHost {
  host_id: string;
  host_name: string;
  os_name?: string;
  os_version?: string;
  arch?: string;
  last_seen_time: string;
  status: 'UP' | 'TELEMETRY_UNAVAILABLE';
  instances: TopologyInstance[];
}
export type HistoryPoint = Record<string, string | number | null> & { sample_time: string };
