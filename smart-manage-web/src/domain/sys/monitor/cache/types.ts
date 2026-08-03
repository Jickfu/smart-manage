export interface ManagedCache {
  name: string;
  displayName: string;
  type: 'LOCAL' | 'REMOTE';
  description: string;
  expireSeconds: number;
  estimatedSize?: number;
  statisticsAvailable: boolean;
  currentNodeOnly: boolean;
}

export interface CacheOverview {
  caches: ManagedCache[];
  collectedAt: string;
}
