import { lazy } from 'react';
import type { DomainExtensions } from '@/domain/common/registry/domainExtensions';
export default {
  views: {
    'inbox.tasks': lazy(() => import('./process/task/TaskCenter')),
    'inbox.tasks.preview': lazy(() => import('./process/task/TaskPreview')),
    'message:workflow.instance:approval': lazy(() => import('./process/task/ApprovalView')),
  },
} satisfies DomainExtensions;
