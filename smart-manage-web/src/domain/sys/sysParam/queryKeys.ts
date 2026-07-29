import type { SysParamListForm } from './types';

export const sysParamQueryKeys = {
  all: ['sys', 'sys-param'] as const,
  lists: () => [...sysParamQueryKeys.all, 'list'] as const,
  list: (params: Partial<SysParamListForm>) => [...sysParamQueryKeys.lists(), params] as const,
  details: () => [...sysParamQueryKeys.all, 'detail'] as const,
  detail: (id?: string) => [...sysParamQueryKeys.details(), id] as const,
};
