import type { ScriptListForm, ScriptLogListForm } from './types';

export const scriptQueryKeys = {
  all: ['sys', 'monitor', 'script'] as const,
  apiMetadata: () => [...scriptQueryKeys.all, 'api-metadata'] as const,
  lists: () => [...scriptQueryKeys.all, 'lists'] as const,
  list: (params: Partial<ScriptListForm>) => [...scriptQueryKeys.lists(), params] as const,
  details: () => [...scriptQueryKeys.all, 'details'] as const,
  detail: (id?: string) => [...scriptQueryKeys.details(), id] as const,
  createNewData: () => [...scriptQueryKeys.all, 'createNewData'] as const,
  logs: () => [...scriptQueryKeys.all, 'logs'] as const,
  logList: (params: Partial<ScriptLogListForm>) =>
    [...scriptQueryKeys.logs(), 'list', params] as const,
  logDetail: (id?: string) => [...scriptQueryKeys.logs(), 'detail', id] as const,
};
