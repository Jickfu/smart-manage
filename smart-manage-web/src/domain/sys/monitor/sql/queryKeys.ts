import type { SqlLogListForm } from './types';

export const sqlQueryKeys = {
  all: ['sys', 'monitor', 'sql'] as const,
  catalog: (keyword: string) => [...sqlQueryKeys.all, 'catalog', keyword] as const,
  catalogDetail: (schemaName?: string, objectName?: string) =>
    [...sqlQueryKeys.all, 'catalog', 'detail', schemaName, objectName] as const,
  logs: () => [...sqlQueryKeys.all, 'logs'] as const,
  logList: (params: Partial<SqlLogListForm>) => [...sqlQueryKeys.logs(), 'list', params] as const,
  logDetails: () => [...sqlQueryKeys.logs(), 'detail'] as const,
  logDetail: (id?: string) => [...sqlQueryKeys.logDetails(), id] as const,
};
