import { defineAccessResource } from '@/domain/common/page/access';

export const threadDiagnosticAccess = defineAccessResource('sys:monitor:thread', {
  access: 'access',
  collect: 'collect',
});
