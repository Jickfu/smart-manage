export interface MonitorInstance {
  instanceId: string;
  hostId: string;
  applicationName: string;
  applicationVersion: string;
  lifecycle: 'ACTIVE' | 'RETIRED';
  online: boolean;
  startTime: string;
  lastSeenTime: string;
  current: boolean;
}
export interface CurrentTelemetry<T> {
  status: 'AVAILABLE' | 'UNAVAILABLE';
  snapshot?: T;
}
export interface TopologyInstance {
  instanceId: string;
  applicationName: string;
  applicationVersion?: string;
  lifecycle: 'ACTIVE' | 'RETIRED';
  lastSeenTime: string;
  retiredAt?: string;
  online: boolean;
  current: boolean;
}
export interface MonitorHost {
  hostId: string;
  hostName: string;
  osName?: string;
  osVersion?: string;
  telemetryStatus: 'UP' | 'TELEMETRY_UNAVAILABLE';
  instances: TopologyInstance[];
}
export interface HostSnapshot {
  hostId: string;
  hostname: string;
  sampleTime: string;
  uptimeMs: number;
  os: { name: string; version: string; arch: string };
  cpu: { usage?: number; loadAverage?: number };
  memory: { total: number; available: number; swapTotal: number; swapUsed: number };
  filesystems: Array<{
    name: string;
    mount: string;
    type: string;
    total: number;
    used: number;
    available: number;
    usage?: number;
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
}
export interface InstanceSnapshot {
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
  cpu: { processUsage?: number };
  memory: {
    heapUsed: number;
    heapCommitted: number;
    heapMax: number;
    nonHeapUsed: number;
    nonHeapCommitted: number;
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
export interface HistoryPoint {
  sampleTime: string;
  cpuUsage?: number;
  memoryUsage?: number;
  filesystemUsage?: number;
  worstMount?: string;
  diskReadBytesPerSecond?: number;
  diskWriteBytesPerSecond?: number;
  networkReceiveBytesPerSecond?: number;
  networkTransmitBytesPerSecond?: number;
  processCpuUsage?: number;
  heapUsage?: number;
  requestRate?: number;
  serverErrorRate?: number;
  p95Ms?: number;
  p99Ms?: number;
  threadCount?: number;
  blockedThreadCount?: number;
  dbPoolUsage?: number;
  dbWaiting?: number;
}
