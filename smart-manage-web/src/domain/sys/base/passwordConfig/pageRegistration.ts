import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
export default definePageRegistrations([
  {
    componentKey: 'sys/base/password-config',
    featureKey: 'sys/base/password-config',
    title: '密码策略',
    pageType: 'EDIT',
    component: lazy(() => import('./PasswordConfigPage')),
  },
]);
