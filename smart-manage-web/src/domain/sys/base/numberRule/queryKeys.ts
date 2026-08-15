import type { NumberRuleListForm, NumberScopeType } from './types';

export const numberRuleQueryKeys = {
  all: ['sys', 'number-rule'] as const,
  lists: () => [...numberRuleQueryKeys.all, 'list'] as const,
  list: (params: Partial<NumberRuleListForm>) => [...numberRuleQueryKeys.lists(), params] as const,
  details: () => [...numberRuleQueryKeys.all, 'detail'] as const,
  detail: (id?: string) => [...numberRuleQueryKeys.details(), id] as const,
  options: (scopeType?: NumberScopeType, referenceKey?: string) =>
    [...numberRuleQueryKeys.all, 'options', scopeType, referenceKey] as const,
  references: () => [...numberRuleQueryKeys.all, 'references'] as const,
};
