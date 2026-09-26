import { lazy } from 'react';
import type { DomainExtensions } from '@/domain/common/registry/domainExtensions';
export default {
  views: { 'approval:demo/office/leave': lazy(() => import('./office/leave/LeaveApprovalView')) },
} satisfies DomainExtensions;
