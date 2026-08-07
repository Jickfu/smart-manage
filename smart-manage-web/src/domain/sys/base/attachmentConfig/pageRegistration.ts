import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/attachment-config',
    title: '附件配置',
    pageType: 'CUSTOM',
    component: lazy(() => import('./AttachmentConfigPage')),
  },
]);
