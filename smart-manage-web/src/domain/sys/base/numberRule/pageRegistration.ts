import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.numberRule,
    featureKey: 'sys/base/number-rule',
    title: '编号规则',
    pageType: 'LIST',
    component: lazy(() => import('./NumberRuleListPage')),
  },
  {
    componentKey: componentKeys.numberRuleEdit,
    featureKey: 'sys/base/number-rule',
    title: '编号规则',
    pageType: 'EDIT',
    component: lazy(() => import('./NumberRuleEditPage')),
  },
]);
