import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.sqlConsole,
    pageType: 'CUSTOM',
    component: lazy(() => import('./SqlConsolePage')),
  },
  {
    componentKey: componentKeys.sqlLog,
    pageType: 'LIST',
    component: lazy(() => import('./SqlLogPage')),
  },
  {
    componentKey: componentKeys.sqlLogDetail,
    pageType: 'EDIT',
    component: lazy(() => import('./SqlLogDetailPage')),
  },
]);
