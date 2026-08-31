import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/openapi-application',
    featureKey: 'sys/base/openapi-application',
    title: '第三方应用',
    pageType: 'LIST',
    component: lazy(() => import('./OpenApiApplicationListPage')),
  },
  {
    componentKey: 'sys/base/openapi-application/edit',
    featureKey: 'sys/base/openapi-application',
    title: '第三方应用',
    pageType: 'EDIT',
    component: lazy(() => import('./OpenApiApplicationEditPage')),
  },
  {
    componentKey: 'sys/base/openapi-catalog',
    featureKey: 'sys/base/openapi-catalog',
    title: 'API 文档',
    pageType: 'LIST',
    component: lazy(() => import('./OpenApiCatalogPage')),
  },
  {
    componentKey: 'sys/base/openapi-invocation',
    featureKey: 'sys/base/openapi-invocation',
    title: '调用监控',
    pageType: 'LIST',
    component: lazy(() => import('./OpenApiInvocationPage')),
  },
]);
