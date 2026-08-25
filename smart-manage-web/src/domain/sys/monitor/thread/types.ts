import type { MonitorInstance } from '../runtime/types';

export type { MonitorInstance };

export interface ThreadCollectForm {
  instanceId?: string;
  sampleMillis?: number;
  limit?: number;
  maxDepth?: number;
}

export interface ThreadItem {
  id: number;
  name: string;
  state: string;
  daemon: boolean;
  priority: number;
  cpuUsage?: number | null;
  blockedCount: number;
  waitedCount: number;
  lockName?: string;
  lockOwnerId?: number;
  lockOwnerName?: string;
  deadlocked: boolean;
  stackTrace: string[];
  lockedMonitors: string[];
  lockedSynchronizers: string[];
}

export interface ThreadDiagnosticResult {
  instanceId: string;
  sampleTime: string;
  sampleMillis?: number;
  threads: ThreadItem[];
}
