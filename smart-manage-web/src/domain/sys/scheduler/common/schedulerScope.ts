export interface SchedulerCatalogNode {
  key: string;
  title: string;
  children: SchedulerCatalogNode[];
}

export interface SchedulerScope {
  domainId?: string;
  appId?: string;
}

/** 节点携带当前领域和应用身份；执行列表选中应用时以应用 ID 为筛选依据。 */
export function parseSchedulerScope(key: string): SchedulerScope {
  const [type, domainId, appId] = key.split(':');
  if (type === 'domain' && domainId) return { domainId };
  if (type === 'app' && domainId && appId) return { domainId, appId };
  return {};
}
