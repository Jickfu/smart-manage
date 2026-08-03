export interface RedisRuntime {
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
export interface RedisKey {
  key: string;
  type: string;
  ttl: number;
  memoryBytes?: number;
  valueReadable: boolean;
}
export interface RedisKeys {
  nextCursor: string;
  finished: boolean;
  records: RedisKey[];
}
export interface RedisValueItem {
  name?: string;
  value?: string;
  score?: number;
  base64: boolean;
}
export interface RedisValue {
  key: string;
  type: string;
  truncated: boolean;
  items: RedisValueItem[];
}
