import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '../../componentKeys';
export default definePageRegistrations([
  {
    componentKey: componentKeys.definitionVersions,
    featureKey: 'workflow/process/definition',
    title: '流程版本',
    pageType: 'LIST',
    component: lazy(() => import('./DefinitionVersionsPage')),
  },
  {
    componentKey: componentKeys.definition,
    featureKey: 'workflow/process/definition',
    title: '流程定义',
    pageType: 'LIST',
    component: lazy(() => import('./DefinitionListPage')),
  },
  {
    componentKey: componentKeys.definitionDesigner,
    featureKey: 'workflow/process/definition',
    title: '流程设计',
    pageType: 'EDIT',
    component: lazy(() => import('./DefinitionDesignerPage')),
  },
]);
