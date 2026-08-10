import type { MonitorInstance } from '../node/types';

export type { MonitorInstance };

export interface SlowSqlStat {
  id: string;
  sql: string;
  executeCount: number;
  executeSuccessCount: number;
  errorCount: number;
  executeMillisTotal: number;
  executeMillisMax: number;
  executeMillisAverage: number;
  concurrentMax: number;
  inTransactionCount: number;
  updateCount: number;
  fetchRowCount: number;
  lastExecuteTime?: string;
}

export interface SlowSqlSnapshot {
  instanceId: string;
  sampleTime: string;
  thresholdMs: number;
  records: SlowSqlStat[];
}
