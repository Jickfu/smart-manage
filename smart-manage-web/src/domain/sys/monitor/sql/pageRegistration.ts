import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.sqlConsole,
    featureKey: 'sys/monitor/sql',
    title: 'SQL 控制台',
    pageType: 'CUSTOM',
    component: lazy(() => import('./SqlConsolePage')),
  },
  {
    componentKey: componentKeys.sqlLog,
    featureKey: 'sys/monitor/sql/log',
    title: 'SQL 执行记录',
    pageType: 'LIST',
    component: lazy(() => import('./SqlLogPage')),
  },
  {
    componentKey: componentKeys.sqlLogDetail,
    featureKey: 'sys/monitor/sql/log',
    title: 'SQL 执行记录',
    pageType: 'EDIT',
    component: lazy(() => import('./SqlLogDetailPage')),
  },
]);
