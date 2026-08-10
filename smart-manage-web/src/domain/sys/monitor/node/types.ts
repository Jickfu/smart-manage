export interface NodeSnapshot {
  instanceId: string;
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
  };
  disk: { mount?: string; total: number; used: number; available: number };
  threads: {
    live: number;
    daemon: number;
    peak: number;
    stateCounts: Record<string, number>;
  };
  gc: Array<{ name: string; collectionCount: number; collectionTimeMs: number }>;
  dataSource: {
    active: number;
    idle: number;
    maxActive: number;
    waiting: number;
    connectCount: number;
    errorCount: number;
  };
  health: {
    status: string;
    components: Array<{ name: string; status: string }>;
  };
}

export interface MonitorInstance {
  instanceId: string;
  applicationVersion: string;
  startTime: string;
  lastSeenTime: string;
  current: boolean;
}
