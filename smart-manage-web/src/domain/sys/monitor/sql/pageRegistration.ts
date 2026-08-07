import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.sqlConsole,
    title: 'SQL 控制台',
    pageType: 'CUSTOM',
    component: lazy(() => import('./SqlConsolePage')),
  },
  {
    componentKey: componentKeys.sqlLog,
    title: 'SQL 执行记录',
    pageType: 'LIST',
    component: lazy(() => import('./SqlLogPage')),
  },
  {
    componentKey: componentKeys.sqlLogDetail,
    title: 'SQL 执行记录',
    pageType: 'EDIT',
    component: lazy(() => import('./SqlLogDetailPage')),
  },
]);
