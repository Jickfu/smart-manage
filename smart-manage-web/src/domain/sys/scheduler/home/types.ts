export interface SchedulerTrend {
  date: string;
  success: number;
  failed: number;
  skipped: number;
}

export interface SchedulerSummary {
  totalJobs: number;
  enabledJobs: number;
  pausedJobs: number;
  runningExecutions: number;
  todayExecutions: number;
  todayFailedExecutions: number;
  trends: SchedulerTrend[];
}
