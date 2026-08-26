export interface MonitorOverview {
  hostTelemetryAvailable: number;
  hostTotal: number;
  applicationOnline: number;
  applicationTotal: number;
  databaseHealth: string;
  redisHealth: string;
  pendingCount: number;
  firingCount: number;
  criticalCount: number;
  currentAbnormal: Array<{
    severity: string;
    ruleCode: string;
    scopeType: string;
    scopeId: string;
    summary: string;
  }>;
  topology: Array<{
    hostId: string;
    hostName: string;
    telemetryStatus: string;
    onlineInstances: number;
    totalInstances: number;
  }>;
}
