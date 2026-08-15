import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  NumberRuleListForm,
  NumberRuleOption,
  NumberRuleSaveForm,
  NumberRuleVO,
  NumberReference,
  NumberRuleSegment,
  NumberScopeType,
} from './types';

export const numberRuleApi = {
  listPage: (form: NumberRuleListForm) =>
    request
      .post<Result<PageData<NumberRuleVO>>>('/sys/base/number-rule/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<NumberRuleVO>>('/sys/base/number-rule/detail', { id })
      .then((response) => response.data.data),
  save: (form: NumberRuleSaveForm) =>
    request
      .post<Result<string>>('/sys/base/number-rule/save', form)
      .then((response) => response.data.data),
  delete: (id: string, version: number) =>
    request
      .post<Result<string>>('/sys/base/number-rule/delete', { id, version })
      .then((response) => response.data.data),
  setEnabled: (ids: string[], enabled: boolean) =>
    request
      .post<Result<string>>(
        enabled ? '/sys/base/number-rule/enable' : '/sys/base/number-rule/disable',
        {
          ids,
        },
      )
      .then((response) => response.data.data),
  setDefault: (id: string) =>
    request
      .post<Result<string>>('/sys/base/number-rule/setDefault', { id })
      .then((response) => response.data.data),
  references: () =>
    request
      .get<Result<NumberReference[]>>('/sys/base/number-rule/references')
      .then((response) => response.data.data),
  options: (scopeType?: NumberScopeType, referenceKey?: string) =>
    request
      .get<Result<NumberRuleOption[]>>('/sys/base/number-rule/options', {
        params: { scopeType, referenceKey },
      })
      .then((response) => response.data.data),
  preview: (referenceKey: string, segments: NumberRuleSegment[], sequenceValue: number) =>
    request
      .post<Result<string>>('/sys/base/number-rule/preview', {
        referenceKey,
        segments,
        sequenceValue,
      })
      .then((response) => response.data.data),
};
