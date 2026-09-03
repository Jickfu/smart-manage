import { defineAccessResource } from '@/domain/common/page/access/access';

export const openApiApplicationAccess = defineAccessResource('sys:base:openapi-application', {
  listPage: 'listPage',
  detail: 'detail',
  save: 'save',
  enable: 'enable',
  credential: 'credential',
  grant: 'grant',
});

export const openApiCatalogAccess = defineAccessResource('sys:base:openapi-catalog', {
  listPage: 'listPage',
  save: 'publish',
  publish: 'publish',
});

export const openApiInvocationAccess = defineAccessResource('sys:base:openapi-invocation', {
  listPage: 'listPage',
  save: 'listPage',
});
