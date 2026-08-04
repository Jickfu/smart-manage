import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/script-console',
    pageType: 'CUSTOM',
    component: lazy(() => import('./ScriptConsolePage')),
  },
  {
    componentKey: 'sys/monitor/script-manage',
    pageType: 'LIST',
    component: lazy(() => import('./ScriptListPage')),
  },
  {
    componentKey: 'sys/monitor/script-manage/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./ScriptEditPage')),
  },
  {
    componentKey: 'sys/monitor/script-log',
    pageType: 'LIST',
    component: lazy(() => import('./ScriptLogPage')),
  },
  {
    componentKey: 'sys/monitor/script-log/detail',
    pageType: 'EDIT',
    component: lazy(() => import('./ScriptLogDetailPage')),
  },
  {
    componentKey: 'sys/monitor/script-help',
    pageType: 'CUSTOM',
    component: lazy(() => import('./ScriptHelpPage')),
  },
]);
