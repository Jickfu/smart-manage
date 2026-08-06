import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.scriptConsole,
    pageType: 'CUSTOM',
    component: lazy(() => import('./ScriptConsolePage')),
  },
  {
    componentKey: componentKeys.scriptManage,
    pageType: 'LIST',
    component: lazy(() => import('./ScriptListPage')),
  },
  {
    componentKey: componentKeys.scriptManageEdit,
    pageType: 'EDIT',
    component: lazy(() => import('./ScriptEditPage')),
  },
  {
    componentKey: componentKeys.scriptLog,
    pageType: 'LIST',
    component: lazy(() => import('./ScriptLogPage')),
  },
  {
    componentKey: componentKeys.scriptLogDetail,
    pageType: 'EDIT',
    component: lazy(() => import('./ScriptLogDetailPage')),
  },
  {
    componentKey: componentKeys.scriptHelp,
    pageType: 'CUSTOM',
    component: lazy(() => import('./ScriptHelpPage')),
  },
]);
