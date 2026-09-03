import { defineAccessResource } from '@/domain/common/page/access/access';

export const orgAccess = defineAccessResource('sys:base:org', {
  listPage: 'listPage',
  detail: 'detail',
  save: 'save',
  enable: 'enable',
  disable: 'disable',
  archive: 'archive',
  unarchive: 'unarchive',
});
