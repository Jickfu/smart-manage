import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/file-config',
    featureKey: 'sys/base/file-config',
    title: '文件配置',
    pageType: 'CUSTOM',
    component: lazy(() => import('./FileConfigPage')),
  },
]);
