import { defineAccessResource } from '@/domain/common/page/access/access';

export const instanceAccess = defineAccessResource('workflow:process:instance', {
  list: 'listPage',
  detail: 'detail',
  suspend: 'suspend',
  resume: 'resume',
  terminate: 'terminate',
  jump: 'jump',
  variables: 'variables',
  scriptRetry: 'script-retry',
});
