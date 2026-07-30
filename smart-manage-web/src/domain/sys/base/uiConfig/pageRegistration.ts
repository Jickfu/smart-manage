import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/ui-config',
    pageType: 'CUSTOM',
    component: lazy(() => import('./UiConfigPage')),
  },
]);
