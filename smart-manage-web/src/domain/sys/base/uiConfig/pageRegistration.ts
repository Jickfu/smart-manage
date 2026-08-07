import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/ui-config',
    title: '界面配置',
    pageType: 'CUSTOM',
    component: lazy(() => import('./UiConfigPage')),
  },
]);
