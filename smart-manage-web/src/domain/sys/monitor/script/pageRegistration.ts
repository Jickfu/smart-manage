import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.scriptConsole,
    featureKey: 'sys/monitor/script',
    title: '脚本控制台',
    pageType: 'CUSTOM',
    component: lazy(() => import('./ScriptConsolePage')),
  },
  {
    componentKey: componentKeys.scriptManage,
    featureKey: 'sys/monitor/script',
    title: '脚本',
    pageType: 'LIST',
    component: lazy(() => import('./ScriptListPage')),
  },
  {
    componentKey: componentKeys.scriptManageEdit,
    featureKey: 'sys/monitor/script',
    title: '脚本',
    pageType: 'EDIT',
    component: lazy(() => import('./ScriptEditPage')),
  },
  {
    componentKey: componentKeys.scriptLog,
    featureKey: 'sys/monitor/script/log',
    title: '脚本执行记录',
    pageType: 'LIST',
    component: lazy(() => import('./ScriptLogPage')),
  },
  {
    componentKey: componentKeys.scriptLogDetail,
    featureKey: 'sys/monitor/script/log',
    title: '脚本执行记录',
    pageType: 'EDIT',
    component: lazy(() => import('./ScriptLogDetailPage')),
  },
  {
    componentKey: componentKeys.scriptHelp,
    featureKey: 'sys/monitor/script',
    title: '脚本使用帮助',
    pageType: 'CUSTOM',
    component: lazy(() => import('./ScriptHelpPage')),
  },
]);
