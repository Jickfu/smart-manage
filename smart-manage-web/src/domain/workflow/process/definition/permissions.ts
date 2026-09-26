import { defineAccessResource } from '@/domain/common/page/access/access';

export const definitionAccess = defineAccessResource('workflow:process:definition', {
  list: 'listPage',
  design: 'design',
  save: 'save',
  publish: 'publish',
});
