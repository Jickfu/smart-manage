export interface ManagedCache {
  name: string;
  displayName: string;
  type: 'LOCAL' | 'REMOTE';
  description: string;
  expireSeconds: number;
  estimatedSize?: number;
  statisticsAvailable: boolean;
  currentNodeOnly: boolean;
  getCount: number;
  hitCount: number;
  missCount: number;
  failCount: number;
  hitRate: number;
  qps: number;
  averageGetTime: number;
}

export interface CacheEntry {
  identity: string;
  storage: 'LOCAL' | 'REDIS';
  cacheName?: string;
  cacheDisplayName: string;
  key: string;
  type: string;
  ttl?: number;
  memoryBytes?: number;
  valueReadable: boolean;
  currentNodeOnly: boolean;
}

export interface CacheEntryKey {
  storage: 'LOCAL' | 'REDIS';
  cacheName?: string;
  key: string;
}

export interface CacheOverview {
  caches: ManagedCache[];
  collectedAt: string;
}

export interface CacheRuntime {
  available: boolean;
  version: string;
  uptimeSeconds: number;
  usedMemoryBytes: number;
  usedMemoryDisplay: string;
  maxMemoryBytes: number;
  connectedClients: number;
  dbSize: number;
  keyspaceHits: number;
  keyspaceMisses: number;
  hitRate?: number;
  database: number;
  collectedAt: string;
}

export interface CacheValueItem {
  name?: string;
  value?: string;
  score?: number;
  base64: boolean;
}

export interface CacheValue {
  key: string;
  type: string;
  truncated: boolean;
  items: CacheValueItem[];
}
