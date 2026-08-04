import type { SqlLogListForm } from './types';

export const sqlQueryKeys = {
  all: ['sys', 'monitor', 'sql'] as const,
  logs: () => [...sqlQueryKeys.all, 'logs'] as const,
  logList: (params: Partial<SqlLogListForm>) => [...sqlQueryKeys.logs(), 'list', params] as const,
  logDetails: () => [...sqlQueryKeys.logs(), 'detail'] as const,
  logDetail: (id?: string) => [...sqlQueryKeys.logDetails(), id] as const,
};
