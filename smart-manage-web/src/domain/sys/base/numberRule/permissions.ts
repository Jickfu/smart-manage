import { defineAccessResource } from '@/domain/common/page/access';

export const numberRuleAccess = defineAccessResource('sys:base:number-rule', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  select: 'select',
  preview: 'preview',
  enable: 'enable',
  disable: 'disable',
});
